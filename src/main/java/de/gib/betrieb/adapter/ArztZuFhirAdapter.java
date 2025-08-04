package de.gib.betrieb.adapter;

import de.gib.betrieb.model.Arzt;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.HashMap;

/**
 * Wandelt Legacy-Arzt-Daten in FHIR Practitioner Resource um
 * Entspricht dem FHIR R4 Practitioner Standard
 */
@Component
public class ArztZuFhirAdapter {

    /**
     * Konvertiert einen Legacy-Arzt zu FHIR Practitioner JSON
     */
    public Map<String, Object> konvertiereZuFhir(Arzt arzt) {
        if (arzt == null) {
            return null;
        }

        Map<String, Object> fhirPractitioner = new HashMap<>();

        // FHIR Basis-Felder
        fhirPractitioner.put("resourceType", "Practitioner");
        fhirPractitioner.put("id", arzt.getArztId().toString());

        // Meta-Informationen
        Map<String, Object> meta = new HashMap<>();
        meta.put("profile", new String[]{"http://hl7.org/fhir/StructureDefinition/Practitioner"});
        fhirPractitioner.put("meta", meta);

        // Identifier (Arzt-ID als Identifikator)
        Map<String, Object> identifier = new HashMap<>();
        identifier.put("system", "http://krankenhaus.de/practitioner-id");
        identifier.put("value", arzt.getArztId().toString());
        fhirPractitioner.put("identifier", new Map[]{identifier});

        // Name zusammensetzen
        Map<String, Object> name = new HashMap<>();
        name.put("use", "official");
        name.put("family", arzt.getNachname());
        name.put("given", new String[]{arzt.getVorname()});
        name.put("prefix", new String[]{"Dr."});
        fhirPractitioner.put("name", new Map[]{name});

        // Qualifikation (Fachrichtung) hinzufügen
        if (arzt.getFachrichtung() != null) {
            Map<String, Object> qualification = new HashMap<>();

            Map<String, Object> code = new HashMap<>();
            Map<String, Object> coding = new HashMap<>();
            coding.put("system", "http://snomed.info/sct");
            coding.put("code", mapFachrichtungZuSnomed(arzt.getFachrichtung()));
            coding.put("display", arzt.getFachrichtung());
            code.put("coding", new Map[]{coding});
            code.put("text", arzt.getFachrichtung());

            qualification.put("code", code);
            fhirPractitioner.put("qualification", new Map[]{qualification});
        }

        // Status aktiv setzen
        fhirPractitioner.put("active", true);

        return fhirPractitioner;
    }

    /**
     * Mappt deutsche Fachrichtungen zu SNOMED CT Codes
     * (Vereinfacht für Demo-Zwecke)
     */
    private String mapFachrichtungZuSnomed(String fachrichtung) {
        switch (fachrichtung) {
            case "Innere Medizin":
                return "419192003";
            case "Chirurgie":
                return "394609007";
            case "Orthopädie":
                return "394801008";
            case "Kardiologie":
                return "394579002";
            case "Neurologie":
                return "394591006";
            case "Radiologie":
                return "394914008";
            default:
                return "309343006"; // Physician
        }
    }

    /**
     * Generiert FHIR-konforme Practitioner-URL
     */
    public String generiereFhirUrl(Long arztId) {
        return "Practitioner/" + arztId;
    }

    /**
     * Erstellt eine Practitioner-Reference für andere FHIR Resources
     */
    public Map<String, Object> erstellePractitionerReference(Arzt arzt) {
        Map<String, Object> reference = new HashMap<>();
        reference.put("reference", "Practitioner/" + arzt.getArztId());
        reference.put("display", "Dr. " + arzt.getVorname() + " " + arzt.getNachname() +
                " (" + arzt.getFachrichtung() + ")");
        return reference;
    }
}