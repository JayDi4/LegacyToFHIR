package de.gib.betrieb.controller;

import de.gib.betrieb.service.FhirAdapterService;
import de.gib.betrieb.service.TestdatenGenerator;
import de.gib.betrieb.model.Patient;
import de.gib.betrieb.datenbank.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestdatenGenerator testdatenGenerator;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FhirAdapterService fhirService;

    /**
     * Status-Check
     */
    @GetMapping("/")
    public String status() {
        return "FHIR Legacy Adapter läuft! Alle Systeme bereit für Testdaten-Generierung.";
    }

    /**
     * Generiert Testdaten
     */
    @PostMapping("/generiere/{anzahl}")
    public String generiereTestdaten(@PathVariable int anzahl) {
        try {
            long vorher = patientRepository.count();
            testdatenGenerator.generiereKompletteDatenbasis(anzahl);
            long nachher = patientRepository.count();

            return String.format("Erfolgreich %d Patienten generiert! " +
                            "Vorher: %d, Nachher: %d Patienten in der DB",
                    anzahl, vorher, nachher);
        } catch (Exception e) {
            return "Fehler bei der Generierung: " + e.getMessage();
        }
    }

    /**
     * Zeigt die letzten 10 Patienten an (nach ID sortiert, neueste zuerst)
     */
    @GetMapping("/letzte10Patienten")
    public List<Patient> getLetzte10Patienten() {
        return patientRepository.findLetzteZehnPatienten();
    }

    /**
     * Statistik
     */
    @GetMapping("/statistik")
    public String getStatistik() {
        try {
            long anzahlPatienten = patientRepository.count();
            if (anzahlPatienten == 0) {
                return "Keine Patienten in der Datenbank. " +
                        "Generiere welche mit: POST /api/test/generiere/10";
            }
            return String.format("Aktuell %d Patienten in der Datenbank", anzahlPatienten);
        } catch (Exception e) {
            return "Fehler beim Zählen: " + e.getMessage();
        }
    }

    /**
     * Suche Patienten nach Nachname
     */
    @GetMapping("/suche/{nachname}")
    public List<Patient> suchePatientenNachNachname(@PathVariable String nachname) {
        return patientRepository.findByNachname(nachname);
    }

    /**
     * Löscht alle Testdaten
     */
    @DeleteMapping("/loeschen")
    public String loescheAlleDaten() {
        try {
            long anzahlVorher = patientRepository.count();
            patientRepository.deleteAll();
            return String.format("Alle %d Testdaten wurden gelöscht", anzahlVorher);
        } catch (Exception e) {
            return "Fehler beim Löschen: " + e.getMessage();
        }
    }

    /**
     * Testet den FHIR-Adapter mit einem zufälligen Patienten
     */
    @GetMapping("/fhir-test")
    public Map<String, Object> testeFhirAdapter() {
        try {
            Map<String, Object> testReslut = new HashMap<>();

            // Statistiken holen
            Map<String, Object> stats = fhirService.getAdapterStatistik();
            testReslut.put("statistiken", stats);

            // Ersten Patienten als FHIR konvertieren
            List<Patient> patienten = patientRepository.findLetzteZehnPatienten();
            if (!patienten.isEmpty()) {
                Patient testPatient = patienten.get(0);
                Map<String, Object> fhirPatient = fhirService.getPatientAlsFhir(testPatient.getPatientenId());
                testReslut.put("beispielPatientFhir", fhirPatient);

                // Befunde des Patienten als FHIR
                List<Map<String, Object>> befunde = fhirService.getBefundeFuerPatient(testPatient.getPatientenId());
                testReslut.put("anzahlBefundeFhir", befunde.size());
                if (!befunde.isEmpty()) {
                    testReslut.put("beispielBefundFhir", befunde.get(0));
                }
            }

            testReslut.put("status", "FHIR-Adapter funktioniert korrekt!");
            return testReslut;

        } catch (Exception e) {
            Map<String, Object> fehler = new HashMap<>();
            fehler.put("status", "FEHLER");
            fehler.put("nachricht", e.getMessage());
            return fehler;
        }
    }

    /**
     * Vergleicht Legacy-Daten mit FHIR-Ausgabe
     */
    @GetMapping("/vergleiche/{patientId}")
    public Map<String, Object> vergleicheLegacyMitFhir(@PathVariable Long patientId) {
        Map<String, Object> vergleich = new HashMap<>();

        try {
            // Legacy-Daten
            Optional<Patient> legacyPatient = patientRepository.findById(patientId);
            if (legacyPatient.isPresent()) {
                vergleich.put("legacyPatient", legacyPatient.get());

                // FHIR-Daten
                Map<String, Object> fhirPatient = fhirService.getPatientAlsFhir(patientId);
                vergleich.put("fhirPatient", fhirPatient);

                // Bundle
                Map<String, Object> bundle = fhirService.getPatientBundle(patientId);
                vergleich.put("fhirBundle", bundle);

                vergleich.put("status", "Vergleich erfolgreich erstellt");
            } else {
                vergleich.put("status", "Patient nicht gefunden");
            }

        } catch (Exception e) {
            vergleich.put("status", "FEHLER: " + e.getMessage());
        }

        return vergleich;
    }

    /**
     * Performance-Test: Misst die Konvertierungszeit für mehrere Patienten
     */
    @GetMapping("/performance-test/{anzahl}")
    public Map<String, Object> performanceTest(@PathVariable int anzahl) {
        Map<String, Object> reslut = new HashMap<>();

        try {
            List<Patient> patienten = patientRepository.findAll();
            if (patienten.size() < anzahl) {
                anzahl = patienten.size();
            }

            long startZeit = System.currentTimeMillis();

            int erfolgreich = 0;
            for (int i = 0; i < anzahl; i++) {
                Patient patient = patienten.get(i);
                Map<String, Object> fhirPatient = fhirService.getPatientAlsFhir(patient.getPatientenId());
                if (fhirPatient != null) {
                    erfolgreich++;
                }
            }

            long endZeit = System.currentTimeMillis();
            long dauerMs = endZeit - startZeit;

            reslut.put("getestetePatienten", anzahl);
            reslut.put("erfolgreichKonvertiert", erfolgreich);
            reslut.put("dauerMillisekunden", dauerMs);
            reslut.put("durchschnittProPatient", dauerMs / (double) anzahl);
            reslut.put("patienenProSekunde", (anzahl * 1000.0) / dauerMs);

        } catch (Exception e) {
            reslut.put("fehler", e.getMessage());
        }

        return reslut;
    }

    /**
     * Erweiterte Hilfe-Seite mit allen verfügbaren Endpunkten
     */
    @GetMapping("/help")
    public String getHilfe() {
        return """
                ===================================================
                FHIR Legacy Adapter - Vollständige API-Übersicht
                ===================================================
                
                LEGACY SYSTEM - Test & Verwaltung:
                ────────────────────────────────────────────────
                Status & Info:
                GET    /api/test/                    - System-Status
                GET    /api/test/help                - Diese Hilfe-Seite
                GET    /api/test/statistik           - Datenbank-Statistiken
                
                Testdaten verwalten:
                POST   /api/test/generiere/{anzahl}  - Testdaten erstellen (z.B. /generiere/50)
                DELETE /api/test/loeschen             - ALLE Testdaten löschen
                
                Legacy-Daten anzeigen:
                GET    /api/test/letzte10Patienten    - Letzte 10 Patienten (Legacy-Format)
                GET    /api/test/suche/{nachname}     - Patienten nach Nachname suchen
                
                FHIR ADAPTER - Konvertierte Daten:
                ────────────────────────────────────────────────
                FHIR System-Info:
                GET    /fhir/                        - FHIR-System Übersicht
                GET    /fhir/metadata                - FHIR CapabilityStatement
                GET    /fhir/stats                   - FHIR-Adapter Statistiken
                
                Patient Resources (FHIR R4):
                GET    /fhir/Patient                 - Alle Patienten als FHIR
                GET    /fhir/Patient/{id}            - Einzelner Patient (z.B. /Patient/1)
                GET    /fhir/Patient/{id}/Bundle     - Komplette Patientendaten als Bundle
                GET    /fhir/Patient/{id}/exists     - Prüft ob Patient existiert
                
                Practitioner Resources (Ärzte):
                GET    /fhir/Practitioner            - Alle Ärzte als FHIR
                GET    /fhir/Practitioner/{id}       - Einzelner Arzt (z.B. /Practitioner/1)
                
                Observation Resources (Befunde):
                GET    /fhir/Observation/{id}        - Einzelner Befund (z.B. /Observation/1)
                GET    /fhir/Patient/{id}/Observation - Alle Befunde eines Patienten
                
                DiagnosticReport Resources (Berichte):
                GET    /fhir/DiagnosticReport/{id}   - Einzelner Bericht (z.B. /DiagnosticReport/1)
                GET    /fhir/Patient/{id}/DiagnosticReport - Alle Berichte eines Patienten
                
                ADAPTER TESTING - Performance & Vergleiche:
                ────────────────────────────────────────────────
                FHIR-Adapter Tests:
                GET    /api/test/fhir-test           - Grundfunktionen des FHIR-Adapters testen
                GET    /api/test/vergleiche/{id}     - Legacy vs FHIR Daten vergleichen
                GET    /api/test/performance-test/{anzahl} - Performance-Test (z.B. /performance-test/100)
                
                DOKUMENTATION:
                ────────────────────────────────────────────────
                Swagger UI:   http://localhost:8090/swagger-ui.html
                OpenAPI JSON: http://localhost:8090/v3/api-docs
                
                SCHNELLSTART-ANLEITUNG:
                ────────────────────────────────────────────────
                1. Testdaten erstellen:    POST /api/test/generiere/50
                2. FHIR-Adapter testen:    GET  /api/test/fhir-test
                3. Patient ansehen:        GET  /fhir/Patient/1
                4. Bundle abrufen:         GET  /fhir/Patient/1/Bundle
                5. Performance messen:     GET  /api/test/performance-test/10
                
                BEISPIEL-URLS für Tests:
                ────────────────────────────────────────────────
                • http://localhost:8090/api/test/generiere/20
                • http://localhost:8090/fhir/Patient/1
                • http://localhost:8090/fhir/Patient/1/Bundle
                • http://localhost:8090/api/test/vergleiche/1
                • http://localhost:8090/api/test/performance-test/50
                
                ===================================================
                """;
    }
}