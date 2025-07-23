package de.gib.betrieb.adapter;

import de.gib.betrieb.model.krankenhaus.Patient;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

/**
 * Wandelt Legacy-Patient-Daten in FHIR Patient Resource um
 * Entspricht dem FHIR R4 Patient Standard
 */
@Component
public class PatientZuFhirAdapter {

    /**
     * Konvertiert einen Legacy-Patienten zu FHIR Patient JSON
     */
    public Map<String, Object> konvertiereZuFhir(Patient patient) {
        if (patient == null) {
            return null;
        }

        Map<String, Object> fhirPatient = new HashMap<>();

        // FHIR Basis-Felder
        fhirPatient.put("resourceType", "Patient");
        fhirPatient.put("id", patient.getPatientenId().toString());

        // Meta-Informationen
        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/Patient"});
        fhirPatient.put("meta", meta);

        // Identifier (Patient-ID als Identifikator)
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/patient-id");
        identifier.put("value", patient.getPatientenId().toString());
        fhirPatient.put("identifier", new Map[]{identifier});

        // Name zusammensetzen
        Map<String, Object> name = new HashMap<>();
        name.put("use", "official");
        name.put("family", patient.getNachname());
        name.put("given", new String[]{patient.getVorname()});
        fhirPatient.put("name", new Map[]{name});

        // Geschlecht konvertieren
        String fhirGender = konvertiereGeschlecht(patient.getGeschlecht());
        if (fhirGender != null) {
            fhirPatient.put("gender", fhirGender);
        }

        // Geburtsdatum formatieren
        if (patient.getGeburtsdatum() != null) {
            String geburtsdatumFormatiert = patient.getGeburtsdatum()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            fhirPatient.put("birthDate", geburtsdatumFormatiert);
        }

        // Status immer aktiv setzen
        fhirPatient.put("active", true);

        return fhirPatient;
    }

    /**
     * Konvertiert deutsches Geschlecht zu FHIR Gender Code
     */
    private String konvertiereGeschlecht(String legacyGeschlecht) {
        if (legacyGeschlecht == null) {
            return "unknown";
        }

        switch (legacyGeschlecht.toLowerCase()) {
            case "männlich":
            case "m":
                return "male";
            case "weiblich":
            case "w":
                return "female";
            case "divers":
            case "d":
                return "other";
            default:
                return "unknown";
        }
    }

    /**
     * Generiert FHIR-konforme Patient-URL
     */
    public String generiereFhirUrl(Long patientId) {
        return "Patient/" + patientId;
    }

    /**
     * Erstellt eine Patient-Reference für andere FHIR Resources
     */
    public Map<String, Object> erstellePatientReference(Patient patient) {
        Map<String, Object> reference = new HashMap<>();
        reference.put("reference", "Patient/" + patient.getPatientenId());
        reference.put("display", patient.getVorname() + " " + patient.getNachname());
        return reference;
    }
}