package com.example.loginapi.dto;

import com.example.loginapi.model.infraestrutura.Paragem;


public class HorarioResponse {

    private Long id;
    private Long linhaId;
    private String linhaNumero;
    private String linhaNome;
    private String paragem;
    private int minutosAte;

    public HorarioResponse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getLinhaNumero() { return linhaNumero; }
    public void setLinhaNumero(String linhaNumero) { this.linhaNumero = linhaNumero; }

    public String getLinhaNome() { return linhaNome; }
    public void setLinhaNome(String linhaNome) { this.linhaNome = linhaNome; }

    public String getParagem() { return paragem; }
    public void setParagem(String paragem) { this.paragem = paragem; }

    public int getMinutosAte() { return minutosAte; }
    public void setMinutosAte(int minutosAte) { this.minutosAte = minutosAte; }
}
