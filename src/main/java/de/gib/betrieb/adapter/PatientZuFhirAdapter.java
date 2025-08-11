package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Patient;
import org.springframework.stereotype.Component;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

// Wandelt Patienten Daten aus DB in FHIR Patient Resource um
@Component
public class PatientZuFhirAdapter {

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

        // Identifier
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/patient-id");
        identifier.put("value", patient.getPatientenId().toString());
        fhirPatient.put("identifier", new Map[]{identifier});

        // Name
        Map<String, Object> name = new HashMap<>();
        name.put("use", "official");
        name.put("family", patient.getNachname());
        name.put("given", new String[]{patient.getVorname()});
        fhirPatient.put("name", new Map[]{name});

        // Geschlecht
        String fhirGender = konvertiereGeschlecht(patient.getGeschlecht());
        if (fhirGender != null) {
            fhirPatient.put("gender", fhirGender);
        }

        // Geburtsdatum
        String geburtsdatumFormatiert = null;
        if (patient.getGeburtsdatum() != null) {
            geburtsdatumFormatiert = patient.getGeburtsdatum().format(DateTimeFormatter.ISO_LOCAL_DATE);
            fhirPatient.put("birthDate", geburtsdatumFormatiert);
        }

        fhirPatient.put("active", true);

        // Narrativtext
        String narr = erstelleNarratixtext.patientSatz(
                patient.getVorname(),
                patient.getNachname(),
                geburtsdatumFormatiert,
                fhirGender,
                patient.getPatientenId().toString()
        );
        fhirPatient.put("text", erstelleNarratixtext.baueText(narr));

        return fhirPatient;
    }

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

    public String generiereFhirUrl(Long patientId) {
        return "Patient/" + patientId;
    }

    public Map<String, Object> erstellePatientReference(Patient patient) {
        Map<String, Object> reference = new HashMap<>();
        reference.put("reference", "Patient/" + patient.getPatientenId());
        reference.put("display", patient.getVorname() + " " + patient.getNachname());
        return reference;
    }
}
