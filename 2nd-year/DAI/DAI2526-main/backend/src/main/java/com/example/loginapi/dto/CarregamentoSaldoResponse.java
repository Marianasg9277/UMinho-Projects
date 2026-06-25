package com.example.loginapi.dto;

import java.math.BigDecimal;

public class CarregamentoSaldoResponse {

    private BigDecimal novoSaldo;
    private String referenciaExterna;
    private String faturaNumero;
    private String mensagem;

    public BigDecimal getNovoSaldo() { return novoSaldo; }
    public void setNovoSaldo(BigDecimal novoSaldo) { this.novoSaldo = novoSaldo; }

    public String getReferenciaExterna() { return referenciaExterna; }
    public void setReferenciaExterna(String referenciaExterna) { this.referenciaExterna = referenciaExterna; }

    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
