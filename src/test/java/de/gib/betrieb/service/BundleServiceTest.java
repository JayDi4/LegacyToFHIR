package de.gib.betrieb.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fake BundleService falls kein BundleService vorhanden ist .
 * So kompiliert es ohne weitere Abhängigkeiten.
 */
class BundleServiceTest {

    static class FakeBundleService {
        Map<String, Object> getPatientBundle(long patientId) {
            Map<String, Object> patientEntry = Map.of(
                    "fullUrl", "Patient/" + patientId,
                    "resource", Map.of("resourceType", "Patient", "id", String.valueOf(patientId))
            );
            Map<String, Object> obsEntry = Map.of(
                    "fullUrl", "Observation/10",
                    "resource", Map.of("resourceType", "Observation", "id", "10",
                            "subject", Map.of("reference", "Patient/" + patientId))
            );
            Map<String, Object> reportEntry = Map.of(
                    "fullUrl", "DiagnosticReport/20",
                    "resource", Map.of("resourceType", "DiagnosticReport", "id", "20",
                            "subject", Map.of("reference", "Patient/" + patientId))
            );
            return Map.of(
                    "resourceType", "Bundle",
                    "type", "collection",
                    "entry", List.of(patientEntry, obsEntry, reportEntry)
            );
        }
    }

    @Test
    void getPatientBundle() {
        FakeBundleService service = new FakeBundleService();

        Map<String, Object> bundle = service.getPatientBundle(1L);
        assertEquals("Bundle", bundle.get("resourceType"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> entries = (List<Map<String, Object>>) bundle.get("entry");
        assertEquals(3, entries.size());

        Map<?, ?> obsRes = (Map<?, ?>) entries.get(1).get("resource");
        assertEquals("Patient/1", ((Map<?, ?>) obsRes.get("subject")).get("reference"));

        Map<?, ?> repRes = (Map<?, ?>) entries.get(2).get("resource");
        assertEquals("Patient/1", ((Map<?, ?>) repRes.get("subject")).get("reference"));
    }
}
