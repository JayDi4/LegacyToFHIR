package de.gib.betrieb.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "standort")
public class Standort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "standort_id")
    private Long standortId;

    @ManyToOne
    @JoinColumn(name = "einrichtungs_id")
    @JsonIgnore  // Referenz zur Einrichtung ignorieren
    private Einrichtung einrichtung;

    private String bezeichnung;
    private String typ;

    // Listen mit @JsonIgnore markieren
    @OneToMany(mappedBy = "standort", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Behandlungsfall> behandlungsfaelle;

    @OneToMany(mappedBy = "standort", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Termin> termine;

    // Konstruktoren
    public Standort() {}

    public Standort(Einrichtung einrichtung, String bezeichnung, String typ) {
        this.einrichtung = einrichtung;
        this.bezeichnung = bezeichnung;
        this.typ = typ;
    }

    // Getter und Setter
    public Long getStandortId() { return standortId; }
    public void setStandortId(Long standortId) { this.standortId = standortId; }

    public Einrichtung getEinrichtung() { return einrichtung; }
    public void setEinrichtung(Einrichtung einrichtung) { this.einrichtung = einrichtung; }

    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }

    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }

    public List<Behandlungsfall> getBehandlungsfaelle() { return behandlungsfaelle; }
    public void setBehandlungsfaelle(List<Behandlungsfall> behandlungsfaelle) {
        this.behandlungsfaelle = behandlungsfaelle;
    }

    public List<Termin> getTermine() { return termine; }
    public void setTermine(List<Termin> termine) { this.termine = termine; }
}

