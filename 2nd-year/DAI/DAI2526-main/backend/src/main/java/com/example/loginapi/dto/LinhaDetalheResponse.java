package com.example.loginapi.dto;

import java.util.List;

public class LinhaDetalheResponse {

    private Long id;
    private String numero;
    private String nome;
    private String origem;
    private String destino;
    private Integer duracaoMin;
    private Integer numParagens;
    private String cor;
    private List<ParagemPercursoResponse> percurso;

    public LinhaDetalheResponse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public Integer getDuracaoMin() { return duracaoMin; }
    public void setDuracaoMin(Integer duracaoMin) { this.duracaoMin = duracaoMin; }

    public Integer getNumParagens() { return numParagens; }
    public void setNumParagens(Integer numParagens) { this.numParagens = numParagens; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public List<ParagemPercursoResponse> getPercurso() { return percurso; }
    public void setPercurso(List<ParagemPercursoResponse> percurso) { this.percurso = percurso; }
}
