package de.gib.betrieb.model.krankenhaus;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "medikament")
public class Medikament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medikament_id")
    private Long medikamentId;

    private String code;
    private String bezeichnung;

    // Beziehungen
    @OneToMany(mappedBy = "medikament", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Verordnung> verordnungen;

    // Konstruktoren
    public Medikament() {}

    public Medikament(String code, String bezeichnung) {
        this.code = code;
        this.bezeichnung = bezeichnung;
    }

    // Getter und Setter
    public Long getMedikamentId() { return medikamentId; }
    public void setMedikamentId(Long medikamentId) { this.medikamentId = medikamentId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }

    public List<Verordnung> getVerordnungen() { return verordnungen; }
    public void setVerordnungen(List<Verordnung> verordnungen) { this.verordnungen = verordnungen; }
}