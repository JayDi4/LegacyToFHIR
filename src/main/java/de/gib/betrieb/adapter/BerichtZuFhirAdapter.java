package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Bericht;
import de.gib.betrieb.model.Befund;
import de.gib.betrieb.datenbank.BefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Component
public class BerichtZuFhirAdapter {

    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    @Autowired
    private BefundRepository befundRepository;

    // FHIR-konforme DateTime
    private static final DateTimeFormatter FHIR_DATETIME_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Wandelt Daten in eine FHIR DiagnosticReport Resource
    public Map<String, Object> konvertiereZuFhir(Bericht bericht) {
        if (bericht == null) {
            return null;
        }

        Map<String, Object> fhirReport = new HashMap<>();

        // Basis
        fhirReport.put("resourceType", "DiagnosticReport");
        fhirReport.put("id", bericht.getBerichtId().toString());

        // Meta
        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/DiagnosticReport"});
        fhirReport.put("meta", meta);

        // Identifier
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/diagnostic-report-id");
        identifier.put("value", bericht.getBerichtId().toString());
        fhirReport.put("identifier", new Map[]{identifier});

        // Status
        fhirReport.put("status", "final");

        // Kategorie
        Map<String, Object> category = new HashMap<>();
        Map<String, Object> categoryCoding = new HashMap<>();
        categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0074");

        String code = bericht.getCode();
        if ("LABORBERICHT".equals(code)) {
            categoryCoding.put("code", "LAB");
            categoryCoding.put("display", "Laboratory");
        } else if ("RADIOLOGIE".equals(code)) {
            categoryCoding.put("code", "RAD");
            categoryCoding.put("display", "Radiology");
        } else {
            categoryCoding.put("code", "OTH");
            categoryCoding.put("display", "Other");
        }

        category.put("coding", new Map[]{categoryCoding});
        fhirReport.put("category", new Map[]{category});

        // Code des Berichts
        Map<String, Object> codeEl = new HashMap<>();
        Map<String, Object> coding = new HashMap<>();
        coding.put("system", "http://krankenhaus.de/report-codes");
        coding.put("code", code);
        coding.put("display", mapCodeZuDisplay(code));
        codeEl.put("coding", new Map[]{coding});
        codeEl.put("text", mapCodeZuDisplay(code));
        fhirReport.put("code", codeEl);

        // Subject
        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getPatient() != null) {
            fhirReport.put("subject",
                    patientAdapter.erstellePatientReference(bericht.getBehandlungsfall().getPatient()));
        }

        // Encounter
        if (bericht.getBehandlungsfall() != null) {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("reference", "Encounter/" + bericht.getBehandlungsfall().getFallId());
            fhirReport.put("encounter", encounter);
        }

        // Zeiten
        String effectiveMitTz = null;
        if (bericht.getErstelltAm() != null) {
            ZonedDateTime zdt = bericht.getErstelltAm().atZone(ZoneId.systemDefault());
            effectiveMitTz = zdt.format(FHIR_DATETIME_TZ);
            fhirReport.put("effectiveDateTime", effectiveMitTz);
        }

        if (bericht.getErstelltAm() != null) {
            Instant instant = bericht.getErstelltAm()
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
            fhirReport.put("issued", instant.toString());
        }

        // Performer
        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getArzt() != null) {
            fhirReport.put("performer", new Map[]{
                    arztAdapter.erstellePractitionerReference(bericht.getBehandlungsfall().getArzt())
            });
        }

        // Zugehörige Observations
        int anzahlErgebnisse = 0;
        if (bericht.getBehandlungsfall() != null) {
            List<Befund> befunde = befundRepository.findByBehandlungsfallFallId(
                    bericht.getBehandlungsfall().getFallId());

            if (befunde != null && !befunde.isEmpty()) {
                List<Map<String, Object>> results = new ArrayList<>();
                for (Befund befund : befunde) {
                    Map<String, Object> resultRef = new HashMap<>();
                    resultRef.put("reference", "Observation/" + befund.getBefundId());
                    resultRef.put("display", mapCodeZuDisplayObservation(befund.getCode()));
                    results.add(resultRef);
                }
                fhirReport.put("result", results);
                anzahlErgebnisse = results.size();
            }
        }

        fhirReport.put("conclusion", "Automatisch generierter Bericht vom Typ: " + mapCodeZuDisplay(code));

        // Narrative
        String narr = erstelleNarratixtext.diagnosticReportSatz(
                mapCodeZuDisplay(code),
                "final",
                (String) fhirReport.get("issued"),
                anzahlErgebnisse
        );
        fhirReport.put("text", erstelleNarratixtext.baueText(narr));

        return fhirReport;
    }

    private String mapCodeZuDisplay(String code) {
        if (code == null) return "Medizinischer Bericht";
        switch (code) {
            case "ARZTBRIEF":     return "Arztbrief";
            case "LABORBERICHT":  return "Laborbericht";
            case "RADIOLOGIE":    return "Radiologischer Bericht";
            case "ENTLASSUNG":    return "Entlassungsbericht";
            default:              return "Medizinischer Bericht (" + code + ")";
        }
    }

    private String mapCodeZuDisplayObservation(String code) {
        if (code == null) return "Observation";
        switch (code) {
            case "LAB-001": return "Blutdruck";
            case "LAB-002": return "Blutzucker";
            default:        return "Observation (" + code + ")";
        }
    }

    public String generiereFhirUrl(Long berichtId) {
        return "DiagnosticReport/" + berichtId;
    }
}
