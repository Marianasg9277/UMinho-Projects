package com.example.loginapi.dto;

import java.math.BigDecimal;

public class MovimentoContaResponse {

    private Long id;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal saldoAntes;
    private BigDecimal saldoDepois;
    private String descricao;
    private String criadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getSaldoAntes() { return saldoAntes; }
    public void setSaldoAntes(BigDecimal saldoAntes) { this.saldoAntes = saldoAntes; }

    public BigDecimal getSaldoDepois() { return saldoDepois; }
    public void setSaldoDepois(BigDecimal saldoDepois) { this.saldoDepois = saldoDepois; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
}
