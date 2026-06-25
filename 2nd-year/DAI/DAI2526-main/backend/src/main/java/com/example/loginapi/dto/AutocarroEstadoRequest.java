package com.example.loginapi.dto;

import jakarta.validation.constraints.NotNull;

public class AutocarroEstadoRequest {

    @NotNull(message = "O campo ativo é obrigatório")
    private Boolean ativo;

    public AutocarroEstadoRequest() {}

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
