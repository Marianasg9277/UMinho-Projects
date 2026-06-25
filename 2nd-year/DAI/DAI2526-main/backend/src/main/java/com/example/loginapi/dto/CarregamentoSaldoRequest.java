package com.example.loginapi.dto;

import java.math.BigDecimal;

public class CarregamentoSaldoRequest {

    private BigDecimal valor;
    private String metodoPagamento;
    private Long cartaoId;
    private String telefone;
    private boolean emitirFaturaComNif;
    private String nif;
    private String emailFatura;

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public Long getCartaoId() { return cartaoId; }
    public void setCartaoId(Long cartaoId) { this.cartaoId = cartaoId; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public boolean isEmitirFaturaComNif() { return emitirFaturaComNif; }
    public void setEmitirFaturaComNif(boolean emitirFaturaComNif) { this.emitirFaturaComNif = emitirFaturaComNif; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getEmailFatura() { return emailFatura; }
    public void setEmailFatura(String emailFatura) { this.emailFatura = emailFatura; }
}
