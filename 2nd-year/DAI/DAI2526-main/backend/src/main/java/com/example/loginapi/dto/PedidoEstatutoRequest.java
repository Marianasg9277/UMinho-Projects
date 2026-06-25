package com.example.loginapi.dto;

import com.example.loginapi.model.clientes.enums.TipoEstatuto;


public class PedidoEstatutoRequest {
    private String tipoEstatuto;
    private String observacoes;

    public String getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(String tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
}
