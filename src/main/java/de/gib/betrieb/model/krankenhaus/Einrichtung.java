package de.gib.betrieb.model.krankenhaus;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "einrichtung")
public class Einrichtung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "einrichtungs_id")
    private Long einrichtungsId;

    private String name;
    private String typ;

    // Beziehungen
    @OneToMany(mappedBy = "einrichtung", cascade = CascadeType.ALL)
    private List<Standort> standorte;

    // Konstruktoren
    public Einrichtung() {}

    public Einrichtung(String name, String typ) {
        this.name = name;
        this.typ = typ;
    }

    // Getter und Setter
    public Long getEinrichtungsId() { return einrichtungsId; }
    public void setEinrichtungsId(Long einrichtungsId) { this.einrichtungsId = einrichtungsId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTyp() { return typ; }
    public void setTyp(String typ) { this.typ = typ; }

    public List<Standort> getStandorte() { return standorte; }
    public void setStandorte(List<Standort> standorte) { this.standorte = standorte; }
}
