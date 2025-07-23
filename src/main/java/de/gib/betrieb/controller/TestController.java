package de.gib.betrieb.controller;

import de.gib.betrieb.service.TestdatenGenerator;
import de.gib.betrieb.model.krankenhaus.Patient;
import de.gib.betrieb.datenbank.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private TestdatenGenerator testdatenGenerator;

    @Autowired
    private PatientRepository patientRepository;

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
        } catch(Exception e) {
            return "Fehler bei der Generierung: " + e.getMessage();
        }
    }

    /**
     * Zeigt erste 5 Patienten an - JETZT OHNE zirkuläre Referenzen
     */
    @GetMapping("/patienten")
    public List<Patient> getErstePatienten() {
        List<Patient> allePatienten = patientRepository.findAll();
        return allePatienten.subList(0, Math.min(5, allePatienten.size()));
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
        } catch(Exception e) {
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
        } catch(Exception e) {
            return "Fehler beim Löschen: " + e.getMessage();
        }
    }

    /**
     * Hilfe-Seite
     */
    @GetMapping("/help")
    public String getHilfe() {
        return """
               FHIR Legacy Adapter - Test-Endpunkte:
               
               Status & Info:
               GET  /api/test/              - Status-Check
               GET  /api/test/hello         - Einfacher Test
               GET  /api/test/help          - Diese Hilfe
               
               Statistiken:
               GET  /api/test/statistik     - Anzahl Patienten
               
               Patienten anzeigen:
               GET  /api/test/patienten     - Erste 5 Patienten
               GET  /api/test/suche/{name}  - Suche nach Nachname
               
               Daten verwalten:
               POST /api/test/generiere/{anzahl} - Testdaten erstellen
               DELETE /api/test/loeschen    - ALLE Daten löschen
               
               Swagger UI: http://localhost:8090/swagger-ui.html
               """;
    }
}