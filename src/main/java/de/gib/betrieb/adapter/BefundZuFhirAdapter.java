package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Befund;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

@Component
public class BefundZuFhirAdapter {

    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    // FHIR-konforme DateTime: mit Sekunden und Zeitzone
    private static final DateTimeFormatter FHIR_DATETIME_TZ =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    // Wandelt Legacy-Befund-Daten in eine FHIR Observation
    public Map<String, Object> konvertiereZuFhir(Befund befund) {
        if (befund == null) {
            return null;
        }

        Map<String, Object> fhirObservation = new HashMap<>();

        // Basis
        fhirObservation.put("resourceType", "Observation");
        fhirObservation.put("id", befund.getBefundId().toString());

        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/Observation"});
        fhirObservation.put("meta", meta);

        // Identifier
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/observation-id");
        identifier.put("value", befund.getBefundId().toString());
        fhirObservation.put("identifier", new Map[]{identifier});

        // Status
        fhirObservation.put("status", "final");

        // Kategorie
        Map<String, Object> category = new HashMap<>();
        Map<String, Object> categoryCoding = new HashMap<>();
        categoryCoding.put("system", "http://terminology.hl7.org/CodeSystem/observation-category");

        if (befund.getCode() != null && befund.getCode().startsWith("LAB")) {
            categoryCoding.put("code", "laboratory");
            categoryCoding.put("display", "Laboratory");
        } else if (befund.getCode() != null && befund.getCode().startsWith("VITAL")) {
            categoryCoding.put("code", "vital-signs");
            categoryCoding.put("display", "Vital Signs");
        } else {
            categoryCoding.put("code", "exam");
            categoryCoding.put("display", "Exam");
        }

        category.put("coding", new Map[]{categoryCoding});
        fhirObservation.put("category", new Map[]{category});

        // Code
        Map<String, Object> code = new HashMap<>();
        Map<String, Object> coding = new HashMap<>();
        coding.put("system", "http://krankenhaus.de/codes");
        coding.put("code", befund.getCode());
        coding.put("display", mapCodeZuDisplay(befund.getCode()));
        code.put("coding", new Map[]{coding});
        code.put("text", mapCodeZuDisplay(befund.getCode()));
        fhirObservation.put("code", code);

        // Subject
        if (befund.getBehandlungsfall() != null && befund.getBehandlungsfall().getPatient() != null) {
            fhirObservation.put("subject",
                    patientAdapter.erstellePatientReference(befund.getBehandlungsfall().getPatient()));
        }

        // Encounter
        if (befund.getBehandlungsfall() != null) {
            Map<String, Object> encounter = new HashMap<>();
            encounter.put("reference", "Encounter/" + befund.getBehandlungsfall().getFallId());
            fhirObservation.put("encounter", encounter);
        }

        // Zeitpunkt
        String zeitMitTz = null;
        if (befund.getZeitpunkt() != null) {
            ZonedDateTime zdt = befund.getZeitpunkt().atZone(ZoneId.systemDefault());
            zeitMitTz = zdt.format(FHIR_DATETIME_TZ);
            fhirObservation.put("effectiveDateTime", zeitMitTz);
        }

        // Wert
        String valueTextForNarrative = null;
        if (befund.getWert() != null) {
            try {
                Double numerischerWert = Double.parseDouble(
                        befund.getWert().replaceAll("[^0-9.,]", "").replace(",", ".")
                );
                Map<String, Object> valueQuantity = new HashMap<>();
                valueQuantity.put("value", numerischerWert);

                if (befund.getEinheit() != null && !befund.getEinheit().isEmpty()) {
                    String einheitDisplay = befund.getEinheit();
                    String ucumCode = mappeNachUcumCode(einheitDisplay);

                    valueQuantity.put("unit", einheitDisplay);
                    valueQuantity.put("system", "http://unitsofmeasure.org");
                    valueQuantity.put("code", ucumCode);


                    valueTextForNarrative = numerischerWert + " " + einheitDisplay;
                } else {
                    valueTextForNarrative = numerischerWert.toString();
                }

                fhirObservation.put("valueQuantity", valueQuantity);
            } catch (NumberFormatException e) {
                fhirObservation.put("valueString", befund.getWert());
                valueTextForNarrative = befund.getWert();
            }
        }

        // Performer
        if (befund.getBehandlungsfall() != null && befund.getBehandlungsfall().getArzt() != null) {
            fhirObservation.put("performer", new Map[]{
                    arztAdapter.erstellePractitionerReference(befund.getBehandlungsfall().getArzt())
            });
        }

        // Narrativtext
        String narr = erstelleNarratixtext.observationSatz(
                mapCodeZuDisplay(befund.getCode()),
                valueTextForNarrative,
                zeitMitTz
        );
        fhirObservation.put("text", erstelleNarratixtext.baueText(narr));

        return fhirObservation;
    }

    // Mappt interne Codes zu lesbaren Bezeichnungen
    private String mapCodeZuDisplay(String code) {
        if (code == null) return "Unbekannte Messung";
        switch (code) {
            case "LAB-001": return "Blutdruck";
            case "LAB-002": return "Blutzucker";
            case "VITAL-001": return "Körpertemperatur";
            case "VITAL-002": return "Puls";
            case "BLOOD-001": return "Blutbild";
            case "URINE-001": return "Urinanalyse";
            default: return "Unbekannte Messung (" + code + ")";
        }
    }

    private String mappeNachUcumCode(String einheitDisplay) {
        if (einheitDisplay == null) return null;
        switch (einheitDisplay) {
            case "mmHg": return "mm[Hg]";
            case "°C":   return "Cel";
            case "degC": return "Cel";
            default:     return einheitDisplay;
        }
    }

    public String generiereFhirUrl(Long befundId) {
        return "Observation/" + befundId;
    }
}
