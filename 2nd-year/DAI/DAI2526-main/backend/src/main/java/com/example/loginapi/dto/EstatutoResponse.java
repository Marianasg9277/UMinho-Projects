package com.example.loginapi.dto;

import com.example.loginapi.model.clientes.enums.TipoEstatuto;


public class EstatutoResponse {
    private String tipoEstatuto;
    private String estado;
    private String dataInicio;
    private String dataFim;
    private boolean automatico;

    public EstatutoResponse() {}
    public EstatutoResponse(String tipoEstatuto, String estado, String dataInicio, String dataFim, boolean automatico) {
        this.tipoEstatuto = tipoEstatuto; this.estado = estado; this.dataInicio = dataInicio;
        this.dataFim = dataFim; this.automatico = automatico;
    }
    /** Constructor for effective (resolved) status without DB record */
    public EstatutoResponse(String tipoEstatuto, boolean automatico) {
        this.tipoEstatuto = tipoEstatuto; this.estado = "EFFECTIVE"; this.automatico = automatico;
    }

    public String getTipoEstatuto() { return tipoEstatuto; }
    public String getEstado() { return estado; }
    public String getDataInicio() { return dataInicio; }
    public String getDataFim() { return dataFim; }
    public boolean isAutomatico() { return automatico; }
}
