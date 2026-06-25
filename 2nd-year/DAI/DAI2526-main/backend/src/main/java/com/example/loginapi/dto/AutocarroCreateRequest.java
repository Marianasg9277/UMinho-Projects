package com.example.loginapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.example.loginapi.model.infraestrutura.Linha;


public class AutocarroCreateRequest {

    @NotBlank(message = "O código é obrigatório")
    @Size(max = 50, message = "O código não pode exceder 50 caracteres")
    private String codigo;

    @Size(max = 100, message = "O nome não pode exceder 100 caracteres")
    private String nome;

    @NotNull(message = "A linha é obrigatória")
    private Long linhaId;

    private Boolean ativo = true;

    public AutocarroCreateRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
