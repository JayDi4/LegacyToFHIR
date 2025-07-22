package service;

import model.krankenhaus.*;
import datenbank.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

@Service
public class TestdatenGenerator {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ArztRepository arztRepository;

    @Autowired
    private EinrichtungRepository einrichtungRepository;

    @Autowired
    private StandortRepository standortRepository;

    @Autowired
    private BehandlungsfallRepository behandlungsfallRepository;

    @Autowired
    private BefundRepository befundRepository;

    @Autowired
    private BerichtRepository berichtRepository;

    @Autowired
    private MedikamentRepository medikamentRepository;

    @Autowired
    private VerordnungRepository verordnungRepository;

    @Autowired
    private TerminRepository terminRepository;

    private Random zufallsGenerator = new Random();

    // Namen für realistische Testdaten
    private String[] vornamenMaennlich = {"Hans", "Peter", "Klaus", "Wolfgang", "Bernd",
            "Michael", "Thomas", "Andreas", "Stefan", "Ralf"};
    private String[] vornamenWeiblich = {"Maria", "Anna", "Petra", "Sabine", "Monika",
            "Andrea", "Claudia", "Karin", "Barbara", "Ute"};
    private String[] nachnamen = {"Müller", "Schmidt", "Schneider", "Fischer", "Weber",
            "Meyer", "Wagner", "Becker", "Schulz", "Hoffmann"};

    private String[] fachrichtungen = {"Innere Medizin", "Chirurgie", "Orthopädie",
            "Kardiologie", "Neurologie", "Radiologie"};

    private String[] einrichtungsNamen = {"Universitätsklinikum", "Stadtkrankenhaus",
            "Kreisklinik", "Fachklinik"};

    private String[] standortBezeichnungen = {"Hauptgebäude", "Neubau", "Ambulanz",
            "Notaufnahme", "Labor", "OP-Zentrum"};

    private String[] befundCodes = {"LAB-001", "LAB-002", "VITAL-001", "VITAL-002",
            "BLOOD-001", "URINE-001"};
    private String[] befundWerte = {"120/80", "98.6", "Normal", "Erhöht", "Niedrig", "7.4"};
    private String[] einheiten = {"mmHg", "°C", "", "mg/dl", "pH", "ml"};

    /**
     * Generiert eine komplette Krankenhaus-Datenbasis mit allen Entitäten
     */
    public void generiereKompletteDatenbasis(int anzahlPatienten) {
        System.out.println("Starte Generierung von " + anzahlPatienten + " Patienten...");

        try {
            // 1. Grunddaten erstellen
            List<Einrichtung> einrichtungen = generiereEinrichtungen();
            List<Standort> standorte = generiereStandorte(einrichtungen);
            List<Arzt> aerzte = generiereAerzte(20);
            List<Medikament> medikamente = generiereMedikamente();

            // 2. Patienten mit allen Daten generieren
            for(int i = 0; i < anzahlPatienten; i++) {
                Patient patient = generierePatient();

                // Pro Patient 1-3 Behandlungsfälle
                int anzahlFaelle = zufallsGenerator.nextInt(3) + 1;
                for(int j = 0; j < anzahlFaelle; j++) {
                    Behandlungsfall fall = generiereBehandlungsfall(patient, aerzte, standorte);

                    // Pro Fall 2-5 Befunde
                    int anzahlBefunde = zufallsGenerator.nextInt(4) + 2;
                    for(int k = 0; k < anzahlBefunde; k++) {
                        generiereBefund(fall);
                    }

                    // Pro Fall 1-2 Berichte
                    int anzahlBerichte = zufallsGenerator.nextInt(2) + 1;
                    for(int l = 0; l < anzahlBerichte; l++) {
                        generiereBericht(fall);
                    }
                }

                // Pro Patient 1-3 Verordnungen
                int anzahlVerordnungen = zufallsGenerator.nextInt(3) + 1;
                for(int m = 0; m < anzahlVerordnungen; m++) {
                    generiereVerordnung(patient, aerzte, medikamente);
                }

                // Pro Patient 1-2 Termine
                int anzahlTermine = zufallsGenerator.nextInt(2) + 1;
                for(int n = 0; n < anzahlTermine; n++) {
                    generiereTermin(patient, aerzte, standorte);
                }

                if(i % 10 == 0) {
                    System.out.println("Fortschritt: " + i + "/" + anzahlPatienten + " Patienten erstellt");
                }
            }

            System.out.println("Testdaten-Generierung abgeschlossen!");

        } catch(Exception e) {
            System.err.println("Fehler bei der Testdaten-Generierung: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generiert Einrichtungen (Krankenhäuser)
     */
    private List<Einrichtung> generiereEinrichtungen() {
        List<Einrichtung> einrichtungen = new ArrayList<>();

        for(int i = 0; i < einrichtungsNamen.length; i++) {
            String name = einrichtungsNamen[i] + " " + (i + 1);
            String typ = i % 2 == 0 ? "Akutkrankenhaus" : "Fachklinik";

            Einrichtung einrichtung = new Einrichtung(name, typ);
            einrichtungen.add(einrichtungRepository.save(einrichtung));
        }

        System.out.println(einrichtungen.size() + " Einrichtungen erstellt");
        return einrichtungen;
    }

    /**
     * Generiert Standorte für die Einrichtungen
     */
    private List<Standort> generiereStandorte(List<Einrichtung> einrichtungen) {
        List<Standort> standorte = new ArrayList<>();

        for(Einrichtung einrichtung : einrichtungen) {
            // Pro Einrichtung 3-5 Standorte
            int anzahlStandorte = zufallsGenerator.nextInt(3) + 3;

            for(int i = 0; i < anzahlStandorte; i++) {
                String bezeichnung = standortBezeichnungen[i % standortBezeichnungen.length];
                String typ = i == 0 ? "Hauptstandort" : "Nebenstandort";

                Standort standort = new Standort(einrichtung, bezeichnung, typ);
                standorte.add(standortRepository.save(standort));
            }
        }

        System.out.println(standorte.size() + " Standorte erstellt");
        return standorte;
    }

    /**
     * Generiert Ärzte
     */
    private List<Arzt> generiereAerzte(int anzahl) {
        List<Arzt> aerzte = new ArrayList<>();

        for(int i = 0; i < anzahl; i++) {
            String vorname = vornamenMaennlich[zufallsGenerator.nextInt(vornamenMaennlich.length)];
            String nachname = nachnamen[zufallsGenerator.nextInt(nachnamen.length)];
            String fachrichtung = fachrichtungen[zufallsGenerator.nextInt(fachrichtungen.length)];

            Arzt arzt = new Arzt(vorname, nachname, fachrichtung);
            aerzte.add(arztRepository.save(arzt));
        }

        System.out.println(aerzte.size() + " Ärzte erstellt");
        return aerzte;
    }

    /**
     * Generiert Medikamente
     */
    private List<Medikament> generiereMedikamente() {
        List<Medikament> medikamente = new ArrayList<>();

        String[] medikamentNamen = {"Aspirin", "Ibuprofen", "Paracetamol", "Amoxicillin",
                "Metformin", "Atorvastatin", "Lisinopril", "Omeprazol"};
        String[] codes = {"MED-001", "MED-002", "MED-003", "MED-004",
                "MED-005", "MED-006", "MED-007", "MED-008"};

        for(int i = 0; i < medikamentNamen.length; i++) {
            Medikament medikament = new Medikament(codes[i], medikamentNamen[i]);
            medikamente.add(medikamentRepository.save(medikament));
        }

        System.out.println(medikamente.size() + " Medikamente erstellt");
        return medikamente;
    }

    /**
     * Generiert einen einzelnen Patienten
     */
    private Patient generierePatient() {
        boolean istMann = zufallsGenerator.nextBoolean();
        String vorname;
        String geschlecht;

        if(istMann) {
            vorname = vornamenMaennlich[zufallsGenerator.nextInt(vornamenMaennlich.length)];
            geschlecht = "männlich";
        } else {
            vorname = vornamenWeiblich[zufallsGenerator.nextInt(vornamenWeiblich.length)];
            geschlecht = "weiblich";
        }

        String nachname = nachnamen[zufallsGenerator.nextInt(nachnamen.length)];

        // Geburtsdatum zwischen 1940 und 2010
        LocalDate geburtsdatum = LocalDate.of(
                1940 + zufallsGenerator.nextInt(70),
                zufallsGenerator.nextInt(12) + 1,
                zufallsGenerator.nextInt(28) + 1
        );

        Patient patient = new Patient(vorname, nachname, geburtsdatum, geschlecht);
        return patientRepository.save(patient);
    }

    /**
     * Generiert einen Behandlungsfall
     */
    private Behandlungsfall generiereBehandlungsfall(Patient patient, List<Arzt> aerzte,
                                                     List<Standort> standorte) {
        Arzt arzt = aerzte.get(zufallsGenerator.nextInt(aerzte.size()));
        Standort standort = standorte.get(zufallsGenerator.nextInt(standorte.size()));

        // Zeitpunkt in den letzten 2 Jahren
        LocalDateTime beginn = LocalDateTime.now().minusDays(zufallsGenerator.nextInt(730));

        String[] artTypen = {"Ambulant", "Stationär", "Notfall", "Voruntersuchung"};
        String art = artTypen[zufallsGenerator.nextInt(artTypen.length)];

        Behandlungsfall fall = new Behandlungsfall(patient, arzt, standort, beginn, art);

        // Bei stationären Fällen ein Enddatum setzen
        if("Stationär".equals(art)) {
            fall.setEnde(beginn.plusDays(zufallsGenerator.nextInt(10) + 1));
        }

        return behandlungsfallRepository.save(fall);
    }

    /**
     * Generiert einen Befund für einen Behandlungsfall
     */
    private Befund generiereBefund(Behandlungsfall fall) {
        String code = befundCodes[zufallsGenerator.nextInt(befundCodes.length)];
        String wert = befundWerte[zufallsGenerator.nextInt(befundWerte.length)];
        String einheit = einheiten[zufallsGenerator.nextInt(einheiten.length)];

        LocalDateTime zeitpunkt = fall.getBeginn().plusMinutes(zufallsGenerator.nextInt(480));

        Befund befund = new Befund(fall, code, wert, einheit, zeitpunkt);
        return befundRepository.save(befund);
    }

    /**
     * Generiert einen Bericht für einen Behandlungsfall
     */
    private Bericht generiereBericht(Behandlungsfall fall) {
        String[] berichtCodes = {"ARZTBRIEF", "LABORBERICHT", "RADIOLOGIE", "ENTLASSUNG"};
        String code = berichtCodes[zufallsGenerator.nextInt(berichtCodes.length)];

        LocalDateTime erstellt = fall.getBeginn().plusHours(zufallsGenerator.nextInt(24));

        Bericht bericht = new Bericht(fall, code, erstellt);
        return berichtRepository.save(bericht);
    }

    /**
     * Generiert eine Medikamentenverordnung
     */
    private Verordnung generiereVerordnung(Patient patient, List<Arzt> aerzte,
                                           List<Medikament> medikamente) {
        Arzt arzt = aerzte.get(zufallsGenerator.nextInt(aerzte.size()));
        Medikament medikament = medikamente.get(zufallsGenerator.nextInt(medikamente.size()));

        LocalDateTime verordnetAm = LocalDateTime.now().minusDays(zufallsGenerator.nextInt(365));

        String[] statusWerte = {"aktiv", "beendet", "pausiert"};
        String status = statusWerte[zufallsGenerator.nextInt(statusWerte.length)];

        String[] dosierungen = {"1x täglich", "2x täglich", "3x täglich", "bei Bedarf"};
        String dosierung = dosierungen[zufallsGenerator.nextInt(dosierungen.length)];

        Verordnung verordnung = new Verordnung(patient, medikament, arzt, verordnetAm, status, dosierung);
        return verordnungRepository.save(verordnung);
    }

    /**
     * Generiert einen Termin
     */
    private Termin generiereTermin(Patient patient, List<Arzt> aerzte, List<Standort> standorte) {
        Arzt arzt = aerzte.get(zufallsGenerator.nextInt(aerzte.size()));
        Standort standort = standorte.get(zufallsGenerator.nextInt(standorte.size()));

        // Termin in der Zukunft (nächste 90 Tage)
        LocalDateTime beginn = LocalDateTime.now().plusDays(zufallsGenerator.nextInt(90));
        LocalDateTime ende = beginn.plusMinutes(30 + zufallsGenerator.nextInt(60)); // 30-90 Min

        String[] statusWerte = {"geplant", "bestätigt", "abgesagt"};
        String status = statusWerte[zufallsGenerator.nextInt(statusWerte.length)];

        Termin termin = new Termin(patient, arzt, standort, beginn, ende, status);
        return terminRepository.save(termin);
    }
}