package model.krankenhaus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verordnung")
public class Verordnung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verordnung_id")
    private Long verordnungId;

    @ManyToOne
    @JoinColumn(name = "patienten_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "medikament_id")
    private Medikament medikament;

    @ManyToOne
    @JoinColumn(name = "arzt_id")
    private Arzt arzt;

    @Column(name = "verordnet_am")
    private LocalDateTime verordnetAm;

    private String status;
    private String dosierung;

    // Konstruktoren
    public Verordnung() {}

    public Verordnung(Patient patient, Medikament medikament, Arzt arzt,
                      LocalDateTime verordnetAm, String status, String dosierung) {
        this.patient = patient;
        this.medikament = medikament;
        this.arzt = arzt;
        this.verordnetAm = verordnetAm;
        this.status = status;
        this.dosierung = dosierung;
    }

    // Getter und Setter
    public Long getVerordnungId() { return verordnungId; }
    public void setVerordnungId(Long verordnungId) { this.verordnungId = verordnungId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medikament getMedikament() { return medikament; }
    public void setMedikament(Medikament medikament) { this.medikament = medikament; }

    public Arzt getArzt() { return arzt; }
    public void setArzt(Arzt arzt) { this.arzt = arzt; }

    public LocalDateTime getVerordnetAm() { return verordnetAm; }
    public void setVerordnetAm(LocalDateTime verordnetAm) { this.verordnetAm = verordnetAm; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDosierung() { return dosierung; }
    public void setDosierung(String dosierung) { this.dosierung = dosierung; }
}
