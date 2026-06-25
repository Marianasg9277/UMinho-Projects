package com.example.loginapi.dto;

public class CriarPasseRequest {
    private Long idCliente;
    private Long tipoPasseId;
    private Long coroaId;

    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public Long getTipoPasseId() { return tipoPasseId; }
    public void setTipoPasseId(Long tipoPasseId) { this.tipoPasseId = tipoPasseId; }
    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }
}
