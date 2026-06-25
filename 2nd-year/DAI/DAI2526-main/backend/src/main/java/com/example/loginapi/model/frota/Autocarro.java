package com.example.loginapi.model.frota;

import jakarta.persistence.*;
import java.time.Instant;
import com.example.loginapi.model.infraestrutura.Linha;


@Entity
@Table(name = "autocarros")
public class Autocarro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único identificador do autocarro (ex: BUS-5784-01) */
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(length = 100)
    private String nome;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = true)
    private Linha linha;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    public Autocarro() {}

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.criadoEm = now;
        this.atualizadoEm = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = Instant.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }

    public Instant getCriadoEm() { return criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
}
