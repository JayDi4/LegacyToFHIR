package de.gib.betrieb.model.krankenhaus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bericht")
public class Bericht {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bericht_id")
    private Long berichtId;

    @ManyToOne
    @JoinColumn(name = "fall_id")
    private Behandlungsfall behandlungsfall;

    private String code;

    @Column(name = "erstellt_am")
    private LocalDateTime erstelltAm;

    // Konstruktoren
    public Bericht() {}

    public Bericht(Behandlungsfall behandlungsfall, String code, LocalDateTime erstelltAm) {
        this.behandlungsfall = behandlungsfall;
        this.code = code;
        this.erstelltAm = erstelltAm;
    }

    // Getter und Setter
    public Long getBerichtId() { return berichtId; }
    public void setBerichtId(Long berichtId) { this.berichtId = berichtId; }

    public Behandlungsfall getBehandlungsfall() { return behandlungsfall; }
    public void setBehandlungsfall(Behandlungsfall behandlungsfall) {
        this.behandlungsfall = behandlungsfall;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getErstelltAm() { return erstelltAm; }
    public void setErstelltAm(LocalDateTime erstelltAm) { this.erstelltAm = erstelltAm; }
}
