package com.example.loginapi.dto;

import java.time.Instant;

public class AutocarroResponse {

    private Long id;
    private String codigo;
    private String nome;
    private Boolean ativo;
    private String linhaNumero;
    private String linhaNome;
    private Instant criadoEm;
    private Instant atualizadoEm;

    public AutocarroResponse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public String getLinhaNumero() { return linhaNumero; }
    public void setLinhaNumero(String linhaNumero) { this.linhaNumero = linhaNumero; }

    public String getLinhaNome() { return linhaNome; }
    public void setLinhaNome(String linhaNome) { this.linhaNome = linhaNome; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
