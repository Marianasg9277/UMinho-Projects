package com.example.loginapi.model.titulos;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "passe_qr_tokens")
public class PasseQrToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "passe_id", nullable = false)
    private Passe passe;

    @Column(name = "token", nullable = false, unique = true, length = 120)
    private String token;

    @Column(name = "gerado_em", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant geradoEm;

    @Column(name = "expira_em", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private Instant expiraEm;

    @Column(name = "revogado_em", columnDefinition = "TIMESTAMPTZ")
    private Instant revogadoEm;

    public PasseQrToken() {}

    public Long getId() { return id; }
    public Passe getPasse() { return passe; }
    public void setPasse(Passe passe) { this.passe = passe; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Instant getGeradoEm() { return geradoEm; }
    public void setGeradoEm(Instant geradoEm) { this.geradoEm = geradoEm; }
    public Instant getExpiraEm() { return expiraEm; }
    public void setExpiraEm(Instant expiraEm) { this.expiraEm = expiraEm; }
    public Instant getRevogadoEm() { return revogadoEm; }
    public void setRevogadoEm(Instant revogadoEm) { this.revogadoEm = revogadoEm; }
}
