package com.example.loginapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class LinhaParagemRequest {

    @NotNull(message = "paragemId é obrigatório")
    private Long paragemId;

    @NotNull(message = "ordem é obrigatória")
    @Min(value = 1, message = "ordem deve ser >= 1")
    private Integer ordem;

    @NotNull(message = "minutosDesdeInicio é obrigatório")
    @Min(value = 0, message = "minutosDesdeInicio deve ser >= 0")
    private Integer minutosDesdeInicio;

    /** "IDA", "VOLTA" ou null. Valores vazios/blank são normalizados para null no service. */
    private String sentido;

    public LinhaParagemRequest() {}

    public Long getParagemId() { return paragemId; }
    public void setParagemId(Long paragemId) { this.paragemId = paragemId; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Integer getMinutosDesdeInicio() { return minutosDesdeInicio; }
    public void setMinutosDesdeInicio(Integer minutosDesdeInicio) { this.minutosDesdeInicio = minutosDesdeInicio; }

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
}
