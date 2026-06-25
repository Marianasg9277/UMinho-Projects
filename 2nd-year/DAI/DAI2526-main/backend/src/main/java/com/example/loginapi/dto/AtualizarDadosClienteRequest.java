package com.example.loginapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AtualizarDadosClienteRequest {

    @NotBlank(message = "O nome e obrigatorio.")
    @Size(min = 2, max = 80, message = "O nome deve ter entre 2 e 80 caracteres.")
    private String nome;

    @NotBlank(message = "O sobrenome e obrigatorio.")
    @Size(min = 2, max = 80, message = "O sobrenome deve ter entre 2 e 80 caracteres.")
    private String sobrenome;

    @NotBlank(message = "A morada e obrigatoria.")
    @Size(min = 5, max = 200, message = "A morada deve ter entre 5 e 200 caracteres.")
    private String morada;

    @NotBlank(message = "O telefone e obrigatorio.")
    @Size(min = 9, max = 9, message = "O telefone deve ter exatamente 9 digitos.")
    @Pattern(regexp = "\\d{9}", message = "O telefone deve ter exatamente 9 digitos.")
    private String telefone;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
