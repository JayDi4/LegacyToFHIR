package de.gib.betrieb.controller;

import de.gib.betrieb.service.FhirAdapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * REST Controller für FHIR-Endpunkte
 * Stellt die Legacy-Daten als FHIR-konforme Resources bereit
 */
@RestController
@RequestMapping("/fhir")
public class FhirController {

    @Autowired
    private FhirAdapterService fhirService;

    /**
     * FHIR-Startseite mit verfügbaren Endpunkten
     */
    @GetMapping("/")
    public Map<String, Object> getFhirInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("system", "FHIR Legacy Adapter");
        info.put("version", "1.0");
        info.put("beschreibung", "Stellt Legacy-Krankenhaus-Daten als FHIR R4 Resources bereit");

        info.put("verfuegbareEndpunkte", new String[]{
                "GET /fhir/Patient - Alle Patienten",
                "GET /fhir/Patient/{id} - Einzelner Patient",
                "GET /fhir/Practitioner - Alle Ärzte",
                "GET /fhir/Practitioner/{id} - Einzelner Arzt",
                "GET /fhir/Observation - Alle Befunde",
                "GET /fhir/Observation/{id} - Einzelner Befund",
                "GET /fhir/DiagnosticReport - Alle Berichte",
                "GET /fhir/DiagnosticReport/{id} - Einzelner Bericht",
                "GET /fhir/Patient/{id}/Bundle - Komplette Patientendaten"
        });

        return info;
    }

    /**
     * Alle Patienten als FHIR Patient Resources
     */
    @GetMapping("/Patient")
    public List<Map<String, Object>> getAllePatienten() {
        return fhirService.getAllePatiententAlsFhir();
    }

    /**
     * Einzelner Patient als FHIR Patient Resource
     */
    @GetMapping("/Patient/{id}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable Long id) {
        Map<String, Object> patient = fhirService.getPatientAlsFhir(id);

        if (patient != null) {
            return ResponseEntity.ok(patient);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Alle Ärzte als FHIR Practitioner Resources
     */
    @GetMapping("/Practitioner")
    public List<Map<String, Object>> getAlleAerzte() {
        return fhirService.getAlleAerzteAlsFhir();
    }

    /**
     * Einzelner Arzt als FHIR Practitioner Resource
     */
    @GetMapping("/Practitioner/{id}")
    public ResponseEntity<Map<String, Object>> getArzt(@PathVariable Long id) {
        Map<String, Object> arzt = fhirService.getArztAlsFhir(id);

        if (arzt != null) {
            return ResponseEntity.ok(arzt);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Einzelner Befund als FHIR Observation Resource
     */
    @GetMapping("/Observation/{id}")
    public ResponseEntity<Map<String, Object>> getBefund(@PathVariable Long id) {
        Map<String, Object> befund = fhirService.getBefundAlsFhir(id);

        if (befund != null) {
            return ResponseEntity.ok(befund);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Alle Befunde eines Patienten als FHIR Observations
     */
    @GetMapping("/Patient/{patientId}/Observation")
    public List<Map<String, Object>> getBefundeVonPatient(@PathVariable Long patientId) {
        return fhirService.getBefundeFuerPatient(patientId);
    }

    /**
     * Einzelner Bericht als FHIR DiagnosticReport Resource
     */
    @GetMapping("/DiagnosticReport/{id}")
    public ResponseEntity<Map<String, Object>> getBericht(@PathVariable Long id) {
        Map<String, Object> bericht = fhirService.getBerichtAlsFhir(id);

        if (bericht != null) {
            return ResponseEntity.ok(bericht);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Alle Berichte eines Patienten als FHIR DiagnosticReports
     */
    @GetMapping("/Patient/{patientId}/DiagnosticReport")
    public List<Map<String, Object>> getBerichteVonPatient(@PathVariable Long patientId) {
        return fhirService.getBerichteFuerPatient(patientId);
    }

    /**
     * Komplettes FHIR Bundle mit allen Daten eines Patienten
     */
    @GetMapping("/Patient/{patientId}/Bundle")
    public ResponseEntity<Map<String, Object>> getPatientBundle(@PathVariable Long patientId) {
        Map<String, Object> bundle = fhirService.getPatientBundle(patientId);

        if (bundle != null && ((List<?>) bundle.get("entry")).size() > 0) {
            return ResponseEntity.ok(bundle);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * FHIR-Adapter Statistiken
     */
    @GetMapping("/stats")
    public Map<String, Object> getStatistiken() {
        return fhirService.getAdapterStatistik();
    }

    /**
     * Prüft ob ein Patient existiert (für FHIR-konforme Validierung)
     */
    @GetMapping("/Patient/{id}/exists")
    public ResponseEntity<Map<String, Object>> checkPatientExists(@PathVariable Long id) {
        Map<String, Object> patient = fhirService.getPatientAlsFhir(id);
        Map<String, Object> reslut = new HashMap<>();

        if (patient != null) {
            reslut.put("exists", true);
            reslut.put("patientId", id);
            reslut.put("resourceType", "Patient");
            return ResponseEntity.ok(reslut);
        } else {
            reslut.put("exists", false);
            reslut.put("patientId", id);
            return ResponseEntity.ok(reslut);
        }
    }

    /**
     * FHIR CapabilityStatement (vereinfacht)
     */
    @GetMapping("/metadata")
    public Map<String, Object> getCapabilityStatement() {
        Map<String, Object> capability = new HashMap<>();
        capability.put("resourceType", "CapabilityStatement");
        capability.put("status", "active");
        capability.put("date", "2025-01-23");
        capability.put("kind", "instance");
        capability.put("software", Map.of(
                "name", "FHIR Legacy Adapter",
                "version", "1.0"
        ));

        // Unterstützte Resources
        capability.put("rest", new Map[]{
                Map.of(
                        "mode", "server",
                        "resource", new Map[]{
                                Map.of("type", "Patient", "interaction", new Map[]{
                                        Map.of("code", "read"), Map.of("code", "search-type")
                                }),
                                Map.of("type", "Practitioner", "interaction", new Map[]{
                                        Map.of("code", "read"), Map.of("code", "search-type")
                                }),
                                Map.of("type", "Observation", "interaction", new Map[]{
                                        Map.of("code", "read"), Map.of("code", "search-type")
                                }),
                                Map.of("type", "DiagnosticReport", "interaction", new Map[]{
                                        Map.of("code", "read"), Map.of("code", "search-type")
                                })
                        }
                )
        });

        return capability;
    }
}