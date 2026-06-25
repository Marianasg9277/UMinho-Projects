package com.example.loginapi.dto;

// Número completo e CVV existem APENAS neste DTO de request — nunca são persistidos.
public class CartaoPagamentoRequest {

    private String nomeTitular;
    private String numeroCartao;
    private String cvv;
    private int mesValidade;
    private int anoValidade;

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getNumeroCartao() { return numeroCartao; }
    public void setNumeroCartao(String numeroCartao) { this.numeroCartao = numeroCartao; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public int getMesValidade() { return mesValidade; }
    public void setMesValidade(int mesValidade) { this.mesValidade = mesValidade; }

    public int getAnoValidade() { return anoValidade; }
    public void setAnoValidade(int anoValidade) { this.anoValidade = anoValidade; }
}
