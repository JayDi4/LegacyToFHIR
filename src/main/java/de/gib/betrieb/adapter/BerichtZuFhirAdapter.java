package de.gib.betrieb.adapter;

import de.gib.betrieb.model.krankenhaus.Bericht;
import de.gib.betrieb.model.krankenhaus.Befund;
import de.gib.betrieb.datenbank.BefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Wandelt Legacy-Bericht-Daten in FHIR DiagnosticReport Resource um
 * Entspricht dem FHIR R4 DiagnosticReport Standard
 */
@Component
public class BerichtZuFhirAdapter {

    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    @Autowired
    private BefundRepository befundRepository;

    /**
     * Konvertiert einen Legacy-Bericht zu FHIR DiagnosticReport JSON
     */
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

        // Subject - Patient-Referenz
        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getPatient() != null) {
            fhirReport.put("subject",
                    patientAdapter.erstellePatientReference(bericht.getBehandlungsfall().getPatient()));
        }

        // Encounter - Behandlungsfall-Referenz
        if (bericht.getBehandlungsfall() != null) {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("reference", "Encounter/" + bericht.getBehandlungsfall().getFallId());
            fhirReport.put("encounter", encounter);
        }

        // Effective DateTime - Zeitpunkt der Erstellung
        if (bericht.getErstelltAm() != null) {
            String zeitpunktFormatiert = bericht.getErstelltAm()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fhirReport.put("effectiveDateTime", zeitpunktFormatiert);
        }

        // Issued - Ausgabezeitpunkt (gleich Erstellungszeitpunkt)
        if (bericht.getErstelltAm() != null) {
            String zeitpunktFormatiert = bericht.getErstelltAm()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fhirReport.put("issued", zeitpunktFormatiert);
        }

        // Performer - wer hat den Bericht erstellt
        if (bericht.getBehandlungsfall() != null && bericht.getBehandlungsfall().getArzt() != null) {
            fhirReport.put("performer", new Map[]{
                    arztAdapter.erstellePractitionerReference(bericht.getBehandlungsfall().getArzt())
            });
        }

        // Result - zugehörige Befunde/Observations
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

        // Conclusion - einfache Zusammenfassung
        fhirReport.put("conclusion", "Automatisch generierter Bericht vom Typ: " +
                mapCodeZuDisplay(bericht.getCode()));

        return fhirReport;
    }

    /**
     * Mappt interne Codes zu lesbaren Bezeichnungen
     */
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

    /**
     * Generiert FHIR-konforme DiagnosticReport-URL
     */
    public String generiereFhirUrl(Long berichtId) {
        return "DiagnosticReport/" + berichtId;
    }
}