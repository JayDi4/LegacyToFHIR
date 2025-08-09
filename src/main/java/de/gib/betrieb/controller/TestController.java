package de.gib.betrieb.controller;

import de.gib.betrieb.service.FhirAdapterService;
import de.gib.betrieb.service.TestdatenGenerator;
import de.gib.betrieb.model.Patient;
import de.gib.betrieb.datenbank.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Anzahl der Patienten in der Datenbank
     */
    @GetMapping("/anzahlPatInDB")
    public String getPatAnzahlInDB() {
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

    @GetMapping(value = "/performance-tests", produces = "text/csv")
    public ResponseEntity<String> performanceTests(
            @RequestParam(name = "durchlaeufe", defaultValue = "100") int durchlaeufe,
            @RequestParam(name = "warmup", defaultValue = "5") int warmup,
            @RequestParam(name = "stichprobe", defaultValue = "100") int stichprobe,
            @RequestParam(name = "parallel", defaultValue = "1") int parallelismus,
            @RequestParam(name = "seed", defaultValue = "42") long seed,
            @RequestParam(name = "mitEinzelzeiten", defaultValue = "false") boolean mitEinzelzeiten,
            @RequestParam(name = "mehrereStichproben", required = false) List<Integer> mehrereStichproben
    ) {
        StringBuilder csv = new StringBuilder();

        csv.append(
                "zeit;stichprobe_n;threads;lauf_nr;"
                        + "dauer_gesamt_ms;durchsatz_pat_pro_s;"
                        + "mittel_ms_pro_patient;median_ms_pro_patient;p95_ms_pro_patient;stdabw_ms_pro_patient;"
                        + "anzahl_angefragt;anzahl_ok;anzahl_fehler\n"
        );

        try {
            List<Patient> alle = patientRepository.findAll();
            if (alle.isEmpty()) {
                return ResponseEntity.badRequest().body("keine Patientendaten vorhanden");
            }
            List<Integer> szenarien = (mehrereStichproben != null && !mehrereStichproben.isEmpty())
                    ? mehrereStichproben
                    : Collections.singletonList(stichprobe);

            Random rnd = new Random(seed);

            final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss.SSS");
            String zeitstempel = LocalDateTime.now().format(TS_FMT);

            for (int s : szenarien) {
                int effS = Math.min(s, alle.size());

                // Warm-up
                for (int i = 0; i < warmup; i++) {
                    fuehreLaufAus(alle, effS, parallelismus, rnd, false);
                }

                // Messläufe
                for (int lauf = 0; lauf < durchlaeufe; lauf++) {
                    LaufErgebnis erg = fuehreLaufAus(alle, effS, parallelismus, rnd, mitEinzelzeiten);

                    double gesamtMs = erg.gesamtNs / 1_000_000.0;
                    double durchsatz = erg.erfolgreich == 0 ? 0.0 : (erg.erfolgreich / (gesamtMs / 1000.0));
                    double mittel = erg.erfolgreich == 0 ? Double.NaN : durchschnitt(erg.einzelzeitenNs) / 1_000_000.0;
                    double median = erg.erfolgreich == 0 ? Double.NaN : perzentil(erg.einzelzeitenNs, 50) / 1_000_000.0;
                    double p95 = erg.erfolgreich == 0 ? Double.NaN : perzentil(erg.einzelzeitenNs, 95) / 1_000_000.0;
                    double stddev = erg.erfolgreich == 0 ? Double.NaN : stddev(erg.einzelzeitenNs) / 1_000_000.0;

                    csv.append(zeitstempel).append(";")
                            .append(effS).append(";")
                            .append(parallelismus).append(";")
                            .append(lauf).append(";")
                            .append(fmt(gesamtMs)).append(";")
                            .append(fmt(durchsatz)).append(";")
                            .append(fmt(mittel)).append(";")
                            .append(fmt(median)).append(";")
                            .append(fmt(p95)).append(";")
                            .append(fmt(stddev)).append(";")
                            .append(effS).append(";")
                            .append(erg.erfolgreich).append(";")
                            .append(erg.fehler).append("\n");

                }
            }

            String dateiname = "perf_" + System.currentTimeMillis() + ".csv";
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + dateiname + "\"")
                    .body(csv.toString());

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("fehler: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    private static class LaufErgebnis {
        long gesamtNs;
        int erfolgreich;
        int fehler;
        List<Long> einzelzeitenNs = new ArrayList<>();
    }

    private LaufErgebnis fuehreLaufAus(List<Patient> alle, int stichprobe, int parallelismus, Random rnd, boolean mitEinzelzeiten) {
        List<Patient> kopie = new ArrayList<>(alle);
        Collections.shuffle(kopie, rnd);
        List<Patient> auswahl = kopie.subList(0, stichprobe);

        ExecutorService pool = (parallelismus <= 1) ? null : Executors.newFixedThreadPool(parallelismus);

        long start = System.nanoTime();
        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger fail = new AtomicInteger(0);
        List<Long> einzelzeitenNs = Collections.synchronizedList(new ArrayList<>(stichprobe));
        List<Future<?>> futures = new ArrayList<>();

        try {
            if (pool == null) {
                // sequentiell: pro Patient Zeit messen und in die Liste schreiben
                for (Patient p : auswahl) {
                    long t0 = System.nanoTime();
                    try {
                        Map<String, Object> fhir = fhirService.getPatientAlsFhir(p.getPatientenId());
                        if (fhir != null) ok.incrementAndGet(); else fail.incrementAndGet();
                    } catch (Exception ex) {
                        fail.incrementAndGet();
                    } finally {
                        long dt = Math.max(1L, System.nanoTime() - t0);
                        einzelzeitenNs.add(dt);
                    }
                }
            } else {
                // parallel: pro Task die Dauer zurückmelden
                for (Patient p : auswahl) {
                    futures.add(pool.submit(() -> {
                        long t0 = System.nanoTime();
                        try {
                            Map<String, Object> fhir = fhirService.getPatientAlsFhir(p.getPatientenId());
                            if (fhir != null) ok.incrementAndGet(); else fail.incrementAndGet();
                        } catch (Exception ex) {
                            fail.incrementAndGet();
                        } finally {
                            long dt = Math.max(1L, System.nanoTime() - t0);
                            einzelzeitenNs.add(dt);
                        }
                        return null;
                    }));
                }
            }
            for (Future<?> f : futures) { try { f.get(); } catch (Exception ignore) {} }
        } finally {
            if (pool != null) pool.shutdown();
        }

        long gesamt = System.nanoTime() - start;

        LaufErgebnis r = new LaufErgebnis();
        r.gesamtNs = gesamt;
        r.erfolgreich = ok.get();
        r.fehler = fail.get();
        r.einzelzeitenNs = einzelzeitenNs; // jetzt immer befüllt
        return r;
    }


    private static double durchschnitt(List<Long> ns) {
        if (ns == null || ns.isEmpty()) return Double.NaN;
        double sum = 0;
        for (long v : ns) sum += v;
        return sum / ns.size();
    }

    private static double perzentil(List<Long> ns, int p) {
        if (ns == null || ns.isEmpty()) return Double.NaN;
        List<Long> s = new ArrayList<>(ns);
        Collections.sort(s);
        int idx = (int) Math.ceil((p / 100.0) * s.size()) - 1;
        idx = Math.max(0, Math.min(idx, s.size() - 1));
        return s.get(idx);
    }

    private static double stddev(List<Long> ns) {
        if (ns == null || ns.isEmpty()) return Double.NaN;
        double mean = durchschnitt(ns);
        double var = 0;
        for (long v : ns) {
            double d = v - mean;
            var += d * d;
        }
        var /= ns.size();
        return Math.sqrt(var);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.US, "%.3f", v);
    }


    /**
     * Erweiterte Hilfe-Seite mit allen verfügbaren Endpunkten (mit ChatGPT erstellt)
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
                GET    /api/test/statistik           - Anzahl der Patienten in der Datenbank
                
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
                GET    /api/test/vergleiche/{id}     - Legacy Daten mit FHIR Daten vergleichen
                GET    /api/test/performance-tests   - Performance-Test (z.B. /performance-test/100)
                
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