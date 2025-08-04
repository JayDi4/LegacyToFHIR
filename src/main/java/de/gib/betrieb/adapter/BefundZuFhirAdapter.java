package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Befund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Wandelt Legacy-Befund-Daten in FHIR Observation Resource um
 * Entspricht dem FHIR R4 Observation Standard
 */
@Component
public class BefundZuFhirAdapter {

    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    /**
     * Konvertiert einen Legacy-Befund zu FHIR Observation JSON
     */
    public Map<String, Object> konvertiereZuFhir(Befund befund) {
        if (befund == null) {
            return null;
        }

        Map<String, Object> fhirObservation = new HashMap<>();

        // FHIR Basis-Felder
        fhirObservation.put("resourceType", "Observation");
        fhirObservation.put("id", befund.getBefundId().toString());

        // Meta-Informationen
        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/Observation"});
        fhirObservation.put("meta", meta);

        // Identifier
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/observation-id");
        identifier.put("value", befund.getBefundId().toString());
        fhirObservation.put("identifier", new Map[]{identifier});

        // Status - immer "final" für abgeschlossene Befunde
        fhirObservation.put("status", "final");

        // Kategorie - Labor/Vital Signs basierend auf Code
        Map<String, Object> category = new HashMap<>();
        Map<String, Object> categoryCoding = new HashMap<>();

        if (befund.getCode().startsWith("LAB")) {
            categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/observation-category");
            categoryCoding.put("code", "laboratory");
            categoryCoding.put("display", "Laboratory");
        } else if (befund.getCode().startsWith("VITAL")) {
            categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/observation-category");
            categoryCoding.put("code", "vital-signs");
            categoryCoding.put("display", "Vital Signs");
        } else {
            categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/observation-category");
            categoryCoding.put("code", "exam");
            categoryCoding.put("display", "Exam");
        }

        category.put("coding", new Map[]{categoryCoding});
        fhirObservation.put("category", new Map[]{category});

        // Code - was wurde gemessen
        Map<String, Object> code = new HashMap<>();
        Map<String, Object> coding = new HashMap<>();
        coding.put("system", "http://krankenhaus.de/codes");
        coding.put("code", befund.getCode());
        coding.put("display", mapCodeZuDisplay(befund.getCode()));
        code.put("coding", new Map[]{coding});
        code.put("text", mapCodeZuDisplay(befund.getCode()));
        fhirObservation.put("code", code);

        // Subject - Patient-Referenz
        if (befund.getBehandlungsfall() != null && befund.getBehandlungsfall().getPatient() != null) {
            fhirObservation.put("subject",
                    patientAdapter.erstellePatientReference(befund.getBehandlungsfall().getPatient()));
        }

        // Encounter - Behandlungsfall-Referenz
        if (befund.getBehandlungsfall() != null) {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("reference", "Encounter/" + befund.getBehandlungsfall().getFallId());
            fhirObservation.put("encounter", encounter);
        }

        // Effective DateTime - Zeitpunkt der Messung
        if (befund.getZeitpunkt() != null) {
            String zeitpunktFormatiert = befund.getZeitpunkt()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            fhirObservation.put("effectiveDateTime", zeitpunktFormatiert);
        }

        // Value - Messwert
        if (befund.getWert() != null) {
            // Versuche numerischen Wert zu erkennen
            try {
                Double numerischerWert = Double.parseDouble(befund.getWert().replaceAll("[^0-9.,]", "").replace(",", "."));

                Map<String, Object> valueQuantity = new HashMap<>();
                valueQuantity.put("value", numerischerWert);
                if (befund.getEinheit() != null && !befund.getEinheit().isEmpty()) {
                    valueQuantity.put("unit", befund.getEinheit());
                    valueQuantity.put("system", "http://unitsofmeasure.org");
                    valueQuantity.put("code", befund.getEinheit());
                }
                fhirObservation.put("valueQuantity", valueQuantity);

            } catch (NumberFormatException e) {
                // Wenn nicht numerisch, als String-Wert behandeln
                fhirObservation.put("valueString", befund.getWert());
            }
        }

        // Performer - wer hat die Messung durchgeführt
        if (befund.getBehandlungsfall() != null && befund.getBehandlungsfall().getArzt() != null) {
            fhirObservation.put("performer", new Map[]{
                    arztAdapter.erstellePractitionerReference(befund.getBehandlungsfall().getArzt())
            });
        }

        return fhirObservation;
    }

    /**
     * Mappt interne Codes zu lesbaren Bezeichnungen
     */
    private String mapCodeZuDisplay(String code) {
        switch (code) {
            case "LAB-001":
                return "Blutdruck";
            case "LAB-002":
                return "Blutzucker";
            case "VITAL-001":
                return "Körpertemperatur";
            case "VITAL-002":
                return "Puls";
            case "BLOOD-001":
                return "Blutbild";
            case "URINE-001":
                return "Urinanalyse";
            default:
                return "Unbekannte Messung (" + code + ")";
        }
    }

    /**
     * Generiert FHIR-konforme Observation-URL
     */
    public String generiereFhirUrl(Long befundId) {
        return "Observation/" + befundId;
    }
}