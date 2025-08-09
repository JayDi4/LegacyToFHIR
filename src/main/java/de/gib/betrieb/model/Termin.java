package de.gib.betrieb.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "termin")
public class Termin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "termin_id")
    private Long terminId;


    @ManyToOne
    @JoinColumn(name = "patienten_id")
    @JsonIgnore
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "arzt_id")
    @JsonIgnore
    private Arzt arzt;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    @JsonIgnore
    private Standort standort;

    @Column(name = "beginn_zeit")
    private LocalDateTime beginnZeit;

    @Column(name = "ende_zeit")
    private LocalDateTime endeZeit;

    private String status;

    public Termin() {}

    public Termin(Patient patient, Arzt arzt, Standort standort,
                  LocalDateTime beginnZeit, LocalDateTime endeZeit, String status) {
        this.patient = patient;
        this.arzt = arzt;
        this.standort = standort;
        this.beginnZeit = beginnZeit;
        this.endeZeit = endeZeit;
        this.status = status;
    }

    // Getter und Setter
    public Long getTerminId() { return terminId; }
    public void setTerminId(Long terminId) { this.terminId = terminId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Arzt getArzt() { return arzt; }
    public void setArzt(Arzt arzt) { this.arzt = arzt; }

    public Standort getStandort() { return standort; }
    public void setStandort(Standort standort) { this.standort = standort; }

    public LocalDateTime getBeginnZeit() { return beginnZeit; }
    public void setBeginnZeit(LocalDateTime beginnZeit) { this.beginnZeit = beginnZeit; }

    public LocalDateTime getEndeZeit() { return endeZeit; }
    public void setEndeZeit(LocalDateTime endeZeit) { this.endeZeit = endeZeit; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}