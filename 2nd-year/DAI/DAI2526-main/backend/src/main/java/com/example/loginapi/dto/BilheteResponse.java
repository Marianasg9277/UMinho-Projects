package com.example.loginapi.dto;

import java.math.BigDecimal;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;


public class BilheteResponse {
    private Long id;
    private String tipoBilheteNome;
    private String linhaNome;
    private BigDecimal quantia;
    private String estadoPagamento;
    private String codigoQr;
    private String faturaNumero;
    private String dataCompra;
    private String validoAte;
    private String coroaNome;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoBilheteNome() { return tipoBilheteNome; }
    public void setTipoBilheteNome(String tipoBilheteNome) { this.tipoBilheteNome = tipoBilheteNome; }
    public String getLinhaNome() { return linhaNome; }
    public void setLinhaNome(String linhaNome) { this.linhaNome = linhaNome; }
    public BigDecimal getQuantia() { return quantia; }
    public void setQuantia(BigDecimal quantia) { this.quantia = quantia; }
    public String getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(String estadoPagamento) { this.estadoPagamento = estadoPagamento; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
    public String getDataCompra() { return dataCompra; }
    public void setDataCompra(String dataCompra) { this.dataCompra = dataCompra; }
    public String getValidoAte() { return validoAte; }
    public void setValidoAte(String validoAte) { this.validoAte = validoAte; }
    public String getCoroaNome() { return coroaNome; }
    public void setCoroaNome(String coroaNome) { this.coroaNome = coroaNome; }
}
