package com.example.loginapi.dto;

import java.math.BigDecimal;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;


public class PagamentoItemResponse {
    private String tipoObjeto;
    private Long objetoId;
    private String titulo;
    private String detalhe;
    private BigDecimal quantia;
    private String estadoPagamento;
    private String faturaNumero;

    public String getTipoObjeto() { return tipoObjeto; }
    public void setTipoObjeto(String tipoObjeto) { this.tipoObjeto = tipoObjeto; }
    public Long getObjetoId() { return objetoId; }
    public void setObjetoId(Long objetoId) { this.objetoId = objetoId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String detalhe) { this.detalhe = detalhe; }
    public BigDecimal getQuantia() { return quantia; }
    public void setQuantia(BigDecimal quantia) { this.quantia = quantia; }
    public String getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(String estadoPagamento) { this.estadoPagamento = estadoPagamento; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
}
