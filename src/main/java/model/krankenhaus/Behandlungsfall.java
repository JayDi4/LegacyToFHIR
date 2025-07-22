package model.krankenhaus;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "behandlungsfall")
public class Behandlungsfall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fall_id")
    private Long fallId;

    @ManyToOne
    @JoinColumn(name = "patienten_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "arzt_id")
    private Arzt arzt;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    private Standort standort;

    private LocalDateTime beginn;
    private LocalDateTime ende;
    private String art;

    // Beziehungen zu Befunden und Berichten
    @OneToMany(mappedBy = "behandlungsfall", cascade = CascadeType.ALL)
    private List<Befund> befunde;

    @OneToMany(mappedBy = "behandlungsfall", cascade = CascadeType.ALL)
    private List<Bericht> berichte;

    // Konstruktoren
    public Behandlungsfall() {}

    public Behandlungsfall(Patient patient, Arzt arzt, Standort standort,
                           LocalDateTime beginn, String art) {
        this.patient = patient;
        this.arzt = arzt;
        this.standort = standort;
        this.beginn = beginn;
        this.art = art;
    }

    // Getter und Setter
    public Long getFallId() { return fallId; }
    public void setFallId(Long fallId) { this.fallId = fallId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Arzt getArzt() { return arzt; }
    public void setArzt(Arzt arzt) { this.arzt = arzt; }

    public Standort getStandort() { return standort; }
    public void setStandort(Standort standort) { this.standort = standort; }

    public LocalDateTime getBeginn() { return beginn; }
    public void setBeginn(LocalDateTime beginn) { this.beginn = beginn; }

    public LocalDateTime getEnde() { return ende; }
    public void setEnde(LocalDateTime ende) { this.ende = ende; }

    public String getArt() { return art; }
    public void setArt(String art) { this.art = art; }

    public List<Befund> getBefunde() { return befunde; }
    public void setBefunde(List<Befund> befunde) { this.befunde = befunde; }

    public List<Bericht> getBerichte() { return berichte; }
    public void setBerichte(List<Bericht> berichte) { this.berichte = berichte; }
}
