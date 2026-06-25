package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;

@Entity
@Table(name = "linhas")
public class Linha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador real da linha no GTFS (routes.txt: route_id). */
    @Column(name = "gtfs_route_id", unique = true, length = 30)
    private String gtfsRouteId;

    @Column(nullable = false, length = 10)
    private String numero;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String origem;

    @Column(nullable = false)
    private String destino;

    @Column(name = "num_paragens", nullable = false)
    private int numParagens;

    @Column(name = "duracao_min", nullable = false)
    private int duracaoMin;

    /** Hex colour used in the UI badge (e.g. "#0ea5e9") */
    @Column(length = 20)
    private String cor;

    /** Soft delete — false = desativada, não aparece nos endpoints públicos */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;

    public Linha() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getGtfsRouteId() { return gtfsRouteId; }
    public void setGtfsRouteId(String gtfsRouteId) { this.gtfsRouteId = gtfsRouteId; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public int getNumParagens() { return numParagens; }
    public void setNumParagens(int numParagens) { this.numParagens = numParagens; }

    public int getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(int duracaoMin) { this.duracaoMin = duracaoMin; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
