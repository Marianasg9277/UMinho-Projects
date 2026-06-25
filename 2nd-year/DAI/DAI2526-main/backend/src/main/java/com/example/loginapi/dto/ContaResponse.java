package com.example.loginapi.dto;

import java.math.BigDecimal;

public class ContaResponse {

    private Long id;
    private BigDecimal saldo;
    private String criadoEm;
    private String atualizadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }

    public String getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(String atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
