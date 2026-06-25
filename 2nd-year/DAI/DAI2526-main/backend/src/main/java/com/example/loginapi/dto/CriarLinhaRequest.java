package com.example.loginapi.dto;

public class CriarLinhaRequest {

    private String numero;
    private String nome;
    private String origem;
    private String destino;
    private int numParagens;
    private int duracaoMin;
    private String gtfsRouteId;
    private String cor;

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

    public String getGtfsRouteId() { return gtfsRouteId; }
    public void setGtfsRouteId(String gtfsRouteId) { this.gtfsRouteId = gtfsRouteId; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }
}
