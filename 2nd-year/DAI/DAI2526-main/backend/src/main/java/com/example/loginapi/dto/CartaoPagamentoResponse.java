package com.example.loginapi.dto;

public class CartaoPagamentoResponse {

    private Long id;
    private String nomeTitular;
    private String ultimos4Digitos;
    private String bandeira;
    private int mesValidade;
    private int anoValidade;
    private boolean predefinido;
    private String criadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getUltimos4Digitos() { return ultimos4Digitos; }
    public void setUltimos4Digitos(String ultimos4Digitos) { this.ultimos4Digitos = ultimos4Digitos; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public int getMesValidade() { return mesValidade; }
    public void setMesValidade(int mesValidade) { this.mesValidade = mesValidade; }

    public int getAnoValidade() { return anoValidade; }
    public void setAnoValidade(int anoValidade) { this.anoValidade = anoValidade; }

    public boolean isPredefinido() { return predefinido; }
    public void setPredefinido(boolean predefinido) { this.predefinido = predefinido; }

    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
}
