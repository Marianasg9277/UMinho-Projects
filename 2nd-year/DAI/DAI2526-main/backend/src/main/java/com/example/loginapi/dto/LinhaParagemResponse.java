package com.example.loginapi.dto;

public class LinhaParagemResponse {

    private Long id;
    private Long linhaId;
    private String linhaNumero;
    private String linhaNome;
    private Long paragemId;
    private String paragemNome;
    private Integer ordem;
    private Integer minutosDesdeInicio;
    private String sentido;

    public LinhaParagemResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getLinhaNumero() { return linhaNumero; }
    public void setLinhaNumero(String linhaNumero) { this.linhaNumero = linhaNumero; }

    public String getLinhaNome() { return linhaNome; }
    public void setLinhaNome(String linhaNome) { this.linhaNome = linhaNome; }

    public Long getParagemId() { return paragemId; }
    public void setParagemId(Long paragemId) { this.paragemId = paragemId; }

    public String getParagemNome() { return paragemNome; }
    public void setParagemNome(String paragemNome) { this.paragemNome = paragemNome; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Integer getMinutosDesdeInicio() { return minutosDesdeInicio; }
    public void setMinutosDesdeInicio(Integer minutosDesdeInicio) { this.minutosDesdeInicio = minutosDesdeInicio; }

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
}
