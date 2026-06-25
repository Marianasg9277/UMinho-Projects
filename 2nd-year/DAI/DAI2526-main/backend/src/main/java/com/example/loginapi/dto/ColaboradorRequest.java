package com.example.loginapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;


public class ColaboradorRequest {

    @NotBlank(message = "nome é obrigatório")
    private String nome;

    @NotBlank(message = "email é obrigatório")
    @Email(message = "email inválido")
    private String email;

    private String morada;

    private String dataNascimento; // ISO-8601: "yyyy-MM-dd", opcional

    private String nif; // opcional; 9 dígitos se preenchido

    @NotNull(message = "tipoColaborador é obrigatório")
    private String tipoColaborador; // "MOTORISTA", "FISCALIZADOR", "GESTOR_SERVICOS"

    private String numeroCarta; // obrigatório se tipoColaborador = MOTORISTA

    private String password; // opcional — se nulo/vazio, o backend gera password temporária

    public ColaboradorRequest() {}

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public String getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getTipoColaborador() { return tipoColaborador; }
    public void setTipoColaborador(String tipoColaborador) { this.tipoColaborador = tipoColaborador; }

    public String getNumeroCarta() { return numeroCarta; }
    public void setNumeroCarta(String numeroCarta) { this.numeroCarta = numeroCarta; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
