package de.gib.betrieb.model.krankenhaus;

import jakarta.persistence.*;
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
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "arzt_id")
    private Arzt arzt;

    @ManyToOne
    @JoinColumn(name = "standort_id")
    private Standort standort;

    private LocalDateTime beginn;
    private LocalDateTime ende;
    private String status;

    // Konstruktoren
    public Termin() {}

    public Termin(Patient patient, Arzt arzt, Standort standort,
                  LocalDateTime beginn, LocalDateTime ende, String status) {
        this.patient = patient;
        this.arzt = arzt;
        this.standort = standort;
        this.beginn = beginn;
        this.ende = ende;
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

    public LocalDateTime getBeginn() { return beginn; }
    public void setBeginn(LocalDateTime beginn) { this.beginn = beginn; }

    public LocalDateTime getEnde() { return ende; }
    public void setEnde(LocalDateTime ende) { this.ende = ende; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

