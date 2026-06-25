package com.example.loginapi.dto;

import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;


public class PagamentoResultadoResponse {
    private boolean success;
    private String tipoObjeto;
    private Long objetoId;
    private String estadoPagamento;
    private String faturaNumero;
    private String mensagem;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getTipoObjeto() { return tipoObjeto; }
    public void setTipoObjeto(String tipoObjeto) { this.tipoObjeto = tipoObjeto; }
    public Long getObjetoId() { return objetoId; }
    public void setObjetoId(Long objetoId) { this.objetoId = objetoId; }
    public String getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(String estadoPagamento) { this.estadoPagamento = estadoPagamento; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
