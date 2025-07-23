package de.gib.betrieb.model.krankenhaus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "befund")
public class Befund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "befund_id")
    private Long befundId;

    @ManyToOne
    @JoinColumn(name = "fall_id")
    private Behandlungsfall behandlungsfall;

    private String code;
    private String wert;
    private String einheit;
    private LocalDateTime zeitpunkt;

    // Konstruktoren
    public Befund() {}

    public Befund(Behandlungsfall behandlungsfall, String code, String wert,
                  String einheit, LocalDateTime zeitpunkt) {
        this.behandlungsfall = behandlungsfall;
        this.code = code;
        this.wert = wert;
        this.einheit = einheit;
        this.zeitpunkt = zeitpunkt;
    }

    // Getter und Setter
    public Long getBefundId() { return befundId; }
    public void setBefundId(Long befundId) { this.befundId = befundId; }

    public Behandlungsfall getBehandlungsfall() { return behandlungsfall; }
    public void setBehandlungsfall(Behandlungsfall behandlungsfall) {
        this.behandlungsfall = behandlungsfall;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getWert() { return wert; }
    public void setWert(String wert) { this.wert = wert; }

    public String getEinheit() { return einheit; }
    public void setEinheit(String einheit) { this.einheit = einheit; }

    public LocalDateTime getZeitpunkt() { return zeitpunkt; }
    public void setZeitpunkt(LocalDateTime zeitpunkt) { this.zeitpunkt = zeitpunkt; }
}
