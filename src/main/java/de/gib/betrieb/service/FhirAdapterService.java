package de.gib.betrieb.service;

import de.gib.betrieb.adapter.*;
import de.gib.betrieb.datenbank.*;
import de.gib.betrieb.model.krankenhaus.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;

/**
 * Haupt-Service für die FHIR-Adapter-Funktionalität
 * Koordiniert die Umwandlung von Legacy-Daten zu FHIR-Ressourcen
 */
@Service
public class FhirAdapterService {

    // Repositories
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ArztRepository arztRepository;

    @Autowired
    private BefundRepository befundRepository;

    @Autowired
    private BerichtRepository berichtRepository;

    @Autowired
    private BehandlungsfallRepository behandlungsfallRepository;

    // Adapter
    @Autowired
    private PatientZuFhirAdapter patientAdapter;

    @Autowired
    private ArztZuFhirAdapter arztAdapter;

    @Autowired
    private BefundZuFhirAdapter befundAdapter;

    @Autowired
    private BerichtZuFhirAdapter berichtAdapter;

    /**
     * Holt einen einzelnen Patienten als FHIR Patient Resource
     */
    public Map<String, Object> getPatientAlsFhir(Long patientId) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isPresent()) {
            return patientAdapter.konvertiereZuFhir(patient.get());
        }
        return null;
    }

    /**
     * Holt alle Patienten als FHIR Patient Resources
     */
    public List<Map<String, Object>> getAllePatiententAlsFhir() {
        List<Patient> patienten = patientRepository.findAll();
        List<Map<String, Object>> fhirPatienten = new ArrayList<>();

        for (Patient patient : patienten) {
            Map<String, Object> fhirPatient = patientAdapter.konvertiereZuFhir(patient);
            if (fhirPatient != null) {
                fhirPatienten.add(fhirPatient);
            }
        }

        return fhirPatienten;
    }

    /**
     * Holt einen einzelnen Arzt als FHIR Practitioner Resource
     */
    public Map<String, Object> getArztAlsFhir(Long arztId) {
        Optional<Arzt> arzt = arztRepository.findById(arztId);
        if (arzt.isPresent()) {
            return arztAdapter.konvertiereZuFhir(arzt.get());
        }
        return null;
    }

    /**
     * Holt alle Ärzte als FHIR Practitioner Resources
     */
    public List<Map<String, Object>> getAlleAerzteAlsFhir() {
        List<Arzt> aerzte = arztRepository.findAll();
        List<Map<String, Object>> fhirAerzte = new ArrayList<>();

        for (Arzt arzt : aerzte) {
            Map<String, Object> fhirArzt = arztAdapter.konvertiereZuFhir(arzt);
            if (fhirArzt != null) {
                fhirAerzte.add(fhirArzt);
            }
        }

        return fhirAerzte;
    }

    /**
     * Holt einen einzelnen Befund als FHIR Observation Resource
     */
    public Map<String, Object> getBefundAlsFhir(Long befundId) {
        Optional<Befund> befund = befundRepository.findById(befundId);
        if (befund.isPresent()) {
            return befundAdapter.konvertiereZuFhir(befund.get());
        }
        return null;
    }

    /**
     * Holt alle Befunde für einen Patienten als FHIR Observations
     */
    public List<Map<String, Object>> getBefundeFuerPatient(Long patientId) {
        // Erst alle Behandlungsfälle des Patienten finden
        List<Behandlungsfall> faelle = behandlungsfallRepository.findByPatientPatientenId(patientId);
        List<Map<String, Object>> fhirBefunde = new ArrayList<>();

        for (Behandlungsfall fall : faelle) {
            List<Befund> befunde = befundRepository.findByBehandlungsfallFallId(fall.getFallId());

            for (Befund befund : befunde) {
                Map<String, Object> fhirBefund = befundAdapter.konvertiereZuFhir(befund);
                if (fhirBefund != null) {
                    fhirBefunde.add(fhirBefund);
                }
            }
        }

        return fhirBefunde;
    }

    /**
     * Holt einen einzelnen Bericht als FHIR DiagnosticReport Resource
     */
    public Map<String, Object> getBerichtAlsFhir(Long berichtId) {
        Optional<Bericht> bericht = berichtRepository.findById(berichtId);
        if (bericht.isPresent()) {
            return berichtAdapter.konvertiereZuFhir(bericht.get());
        }
        return null;
    }

    /**
     * Holt alle Berichte für einen Patienten als FHIR DiagnosticReports
     */
    public List<Map<String, Object>> getBerichteFuerPatient(Long patientId) {
        // Erst alle Behandlungsfälle des Patienten finden
        List<Behandlungsfall> faelle = behandlungsfallRepository.findByPatientPatientenId(patientId);
        List<Map<String, Object>> fhirBerichte = new ArrayList<>();

        for (Behandlungsfall fall : faelle) {
            // Für jeden Fall die Berichte holen
            for (Bericht bericht : fall.getBerichte()) {
                Map<String, Object> fhirBericht = berichtAdapter.konvertiereZuFhir(bericht);
                if (fhirBericht != null) {
                    fhirBerichte.add(fhirBericht);
                }
            }
        }

        return fhirBerichte;
    }

    /**
     * Erstellt ein FHIR Bundle mit allen Daten eines Patienten
     */
    public Map<String, Object> getPatientBundle(Long patientId) {
        Map<String, Object> bundle = new HashMap<>();
        bundle.put("resourceType", "Bundle");
        bundle.put("id", "patient-bundle-" + patientId);
        bundle.put("type", "collection");

        List<Map<String, Object>> entries = new ArrayList<>();

        // Patient hinzufügen
        Map<String, Object> patientFhir = getPatientAlsFhir(patientId);
        if (patientFhir != null) {
            Map<String, Object> patientEntry = new HashMap<>();
            patientEntry.put("resource", patientFhir);
            entries.add(patientEntry);
        }

        // Befunde hinzufügen
        List<Map<String, Object>> befunde = getBefundeFuerPatient(patientId);
        for (Map<String, Object> befund : befunde) {
            Map<String, Object> befundEntry = new HashMap<>();
            befundEntry.put("resource", befund);
            entries.add(befundEntry);
        }

        // Berichte hinzufügen
        List<Map<String, Object>> berichte = getBerichteFuerPatient(patientId);
        for (Map<String, Object> bericht : berichte) {
            Map<String, Object> berichtEntry = new HashMap<>();
            berichtEntry.put("resource", bericht);
            entries.add(berichtEntry);
        }

        bundle.put("entry", entries);
        bundle.put("total", entries.size());

        return bundle;
    }

    /**
     * Statistik-Methode für den Adapter
     */
    public Map<String, Object> getAdapterStatistik() {
        Map<String, Object> statistik = new HashMap<>();

        long anzahlPatienten = patientRepository.count();
        long anzahlAerzte = arztRepository.count();
        long anzahlBefunde = befundRepository.count();
        long anzahlBerichte = berichtRepository.count();

        statistik.put("verfuegbarePatienten", anzahlPatienten);
        statistik.put("verfuegbareAerzte", anzahlAerzte);
        statistik.put("verfuegbareBefunde", anzahlBefunde);
        statistik.put("verfuegbareBerichte", anzahlBerichte);

        statistik.put("unterstuetzteFhirResourcen", new String[]{
                "Patient", "Practitioner", "Observation", "DiagnosticReport"
        });

        return statistik;
    }
}