package com.example.loginapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.loginapi.model.infraestrutura.Paragem;
import com.example.loginapi.model.infraestrutura.Linha;


public class HorarioRequest {

    @NotNull(message = "A linha é obrigatória")
    private Long linhaId;

    @NotBlank(message = "A paragem é obrigatória")
    @Size(max = 255, message = "O nome da paragem não pode exceder 255 caracteres")
    private String paragem;

    @NotNull(message = "Os minutos até chegada são obrigatórios")
    @Min(value = 0, message = "Os minutos até chegada não podem ser negativos")
    private Integer minutosAte;

    public HorarioRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getParagem() { return paragem; }
    public void setParagem(String paragem) { this.paragem = paragem; }

    public Integer getMinutosAte() { return minutosAte; }
    public void setMinutosAte(Integer minutosAte) { this.minutosAte = minutosAte; }
}
