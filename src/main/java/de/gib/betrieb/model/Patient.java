package de.gib.betrieb.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patienten_id")
    private Long patientenId;

    private String vorname;
    private String nachname;
    private LocalDate geburtsdatum;
    private String geschlecht;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonIgnore  // <- WICHTIG: Verhindert zirkuläre JSON-Referenzen (siehe Erklärung in Bachelorarbeit)
    private List<Behandlungsfall> behandlungsfaelle;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Verordnung> verordnungen;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Termin> termine;

    // Konstruktoren
    public Patient() {}

    public Patient(String vorname, String nachname, LocalDate geburtsdatum, String geschlecht) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.geburtsdatum = geburtsdatum;
        this.geschlecht = geschlecht;
    }

    public Long getPatientenId() { return patientenId; }
    public void setPatientenId(Long patientenId) { this.patientenId = patientenId; }

    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }

    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }

    public LocalDate getGeburtsdatum() { return geburtsdatum; }
    public void setGeburtsdatum(LocalDate geburtsdatum) { this.geburtsdatum = geburtsdatum; }

    public String getGeschlecht() { return geschlecht; }
    public void setGeschlecht(String geschlecht) { this.geschlecht = geschlecht; }

    public List<Behandlungsfall> getBehandlungsfaelle() { return behandlungsfaelle; }
    public void setBehandlungsfaelle(List<Behandlungsfall> behandlungsfaelle) {
        this.behandlungsfaelle = behandlungsfaelle;
    }

    public List<Verordnung> getVerordnungen() { return verordnungen; }
    public void setVerordnungen(List<Verordnung> verordnungen) { this.verordnungen = verordnungen; }

    public List<Termin> getTermine() { return termine; }
    public void setTermine(List<Termin> termine) { this.termine = termine; }

    @Override
    public String toString() {
        return "Patient{" +
                "patientenId=" + patientenId +
                ", vorname='" + vorname + '\'' +
                ", nachname='" + nachname + '\'' +
                ", geburtsdatum=" + geburtsdatum +
                ", geschlecht='" + geschlecht + '\'' +
                '}';
    }
}