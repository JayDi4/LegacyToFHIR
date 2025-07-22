package controller;

import service.TestdatenGenerator;
import model.krankenhaus.Patient;
import datenbank.PatientRepository;
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
     * Generiert Testdaten
     * Beispiel: POST /api/test/generiere/50
     */
    @PostMapping("/generiere/{anzahl}")
    public String generiereTestdaten(@PathVariable int anzahl) {
        try {
            testdatenGenerator.generiereKompletteDatenbasis(anzahl);
            return "Erfolgreich " + anzahl + " Patienten mit allen Daten generiert!";
        } catch(Exception e) {
            return "Fehler bei der Generierung: " + e.getMessage();
        }
    }

    /**
     * Zeigt alle Patienten an
     * Beispiel: GET /api/test/patienten
     */
    @GetMapping("/patienten")
    public List<Patient> getAllePatienten() {
        return patientRepository.findAll();
    }

    /**
     * Zählt alle Datensätze
     * Beispiel: GET /api/test/statistik
     */
    @GetMapping("/statistik")
    public String getStatistik() {
        long anzahlPatienten = patientRepository.count();
        return "Aktuell " + anzahlPatienten + " Patienten in der Datenbank";
    }

    /**
     * Löscht alle Testdaten
     * Beispiel: DELETE /api/test/loeschen
     */
    @DeleteMapping("/loeschen")
    public String loescheAlleDaten() {
        try {
            patientRepository.deleteAll();
            return "Alle Testdaten wurden gelöscht";
        } catch(Exception e) {
            return "Fehler beim Löschen: " + e.getMessage();
        }
    }
}
