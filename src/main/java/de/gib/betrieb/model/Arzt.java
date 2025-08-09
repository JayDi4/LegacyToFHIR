package de.gib.betrieb.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "arzt")
public class Arzt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "arzt_id")
    private Long arztId;

    private String vorname;
    private String nachname;
    private String fachrichtung;

    @OneToMany(mappedBy = "arzt", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Behandlungsfall> behandlungsfaelle;

    @OneToMany(mappedBy = "arzt", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Verordnung> verordnungen;

    @OneToMany(mappedBy = "arzt", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Termin> termine;

    public Arzt() {}

    public Arzt(String vorname, String nachname, String fachrichtung) {
        this.vorname = vorname;
        this.nachname = nachname;
        this.fachrichtung = fachrichtung;
    }

    // Getter und Setter
    public Long getArztId() { return arztId; }
    public void setArztId(Long arztId) { this.arztId = arztId; }

    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }

    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }

    public String getFachrichtung() { return fachrichtung; }
    public void setFachrichtung(String fachrichtung) { this.fachrichtung = fachrichtung; }

    public List<Behandlungsfall> getBehandlungsfaelle() { return behandlungsfaelle; }
    public void setBehandlungsfaelle(List<Behandlungsfall> behandlungsfaelle) {
        this.behandlungsfaelle = behandlungsfaelle;
    }

    public List<Verordnung> getVerordnungen() { return verordnungen; }
    public void setVerordnungen(List<Verordnung> verordnungen) { this.verordnungen = verordnungen; }

    public List<Termin> getTermine() { return termine; }
    public void setTermine(List<Termin> termine) { this.termine = termine; }
}
