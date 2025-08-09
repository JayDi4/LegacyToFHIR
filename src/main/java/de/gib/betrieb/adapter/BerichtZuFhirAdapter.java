package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Bericht;
import de.gib.betrieb.model.Befund;
import de.gib.betrieb.datenbank.BefundRepository;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


 // Wandelt Daten aus PostgreDB in FHIR DiagnosticReport Resource um
@Component
public class BerichtZuFhirAdapter {

    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    @Autowired
    private BefundRepository befundRepository;

    public Map<String, Object> konvertiereZuFhir(Bericht bericht) {
        if (bericht == null) {
            return null;
        }

        Map<String, Object> fhirReport = new HashMap<>();

        // FHIR Basis-Felder
        fhirReport.put("resourceType", "DiagnosticReport");
        fhirReport.put("id", bericht.getBerichtId().toString());

        // Meta-Informationen
        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/DiagnosticReport"});
        fhirReport.put("meta", meta);

        // Identifier
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/diagnostic-report-id");
        identifier.put("value", bericht.getBerichtId().toString());
        fhirReport.put("identifier", new Map[]{identifier});

        // Status - immer "final" für fertige Berichte
        fhirReport.put("status", "final");

        // Kategorie basierend auf Bericht-Code
        Map<String, Object> category = new HashMap<>();
        Map<String, Object> categoryCoding = new HashMap<>();

        switch (bericht.getCode()) {
            case "LABORBERICHT":
                categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0074");
                categoryCoding.put("code", "LAB");
                categoryCoding.put("display", "Laboratory");
                break;
            case "RADIOLOGIE":
                categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0074");
                categoryCoding.put("code", "RAD");
                categoryCoding.put("display", "Radiology");
                break;
            default:
                categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/v2-0074");
                categoryCoding.put("code", "OTH");
                categoryCoding.put("display", "Other");
                break;
        }

        category.put("coding", new Map[]{categoryCoding});
        fhirReport.put("category", new Map[]{category});

        // Code - Art des Berichts
        Map<String, Object> code = new HashMap<>();
        Map<String, Object> coding = new HashMap<>();
        coding.put("system", "http://krankenhaus.de/report-codes");
        coding.put("code", bericht.getCode());
        coding.put("display", mapCodeZuDisplay(bericht.getCode()));
        code.put("coding", new Map[]{coding});
        code.put("text", mapCodeZuDisplay(bericht.getCode()));
        fhirReport.put("code", code);

        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getPatient() != null) {
            fhirReport.put("subject",
                    patientAdapter.erstellePatientReference(bericht.getBehandlungsfall().getPatient()));
        }

        if (bericht.getBehandlungsfall() != null) {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("reference", "Encounter/" + bericht.getBehandlungsfall().getFallId());
            fhirReport.put("encounter", encounter);
        }

        //  Zeitpunkt der Erstellung
        if (bericht.getErstelltAm() != null) {
            String zeitpunktFormatiert = bericht.getErstelltAm()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fhirReport.put("effectiveDateTime", zeitpunktFormatiert);
        }

        //  Ausgabezeitpunkt
        if (bericht.getErstelltAm() != null) {
            String zeitpunktFormatiert = bericht.getErstelltAm()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fhirReport.put("issued", zeitpunktFormatiert);
        }

        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getArzt() != null) {
            fhirReport.put("performer", new Map[]{
                    arztAdapter.erstellePractitionerReference(bericht.getBehandlungsfall().getArzt())
            });
        }

        //  zugehörige Befunde/Observations
        if (bericht.getBehandlungsfall() != null) {
            List<Befund> befunde = befundRepository.findByBehandlungsfallFallId(
                    bericht.getBehandlungsfall().getFallId());

            if (!befunde.isEmpty()) {
                List<Map<String, Object>> resluts = new ArrayList<>();
                for (Befund befund : befunde) {
                    Map<String, Object> resultRef = new HashMap<>();
                    resultRef.put("reference", "Observation/" + befund.getBefundId());
                    resultRef.put("display", mapCodeZuDisplay(befund.getCode()));
                    resluts.add(resultRef);
                }
                fhirReport.put("result", resluts);
            }
        }

        // kurze knappe Zusammenfassung
        fhirReport.put("conclusion", "Automatisch generierter Bericht vom Typ: " +
                mapCodeZuDisplay(bericht.getCode()));

        return fhirReport;
    }

    private String mapCodeZuDisplay(String code) {
        switch (code) {
            case "ARZTBRIEF":
                return "Arztbrief";
            case "LABORBERICHT":
                return "Laborbericht";
            case "RADIOLOGIE":
                return "Radiologischer Bericht";
            case "ENTLASSUNG":
                return "Entlassungsbericht";
            default:
                return "Medizinischer Bericht (" + code + ")";
        }
    }

    public String generiereFhirUrl(Long berichtId) {
        return "DiagnosticReport/" + berichtId;
    }
}