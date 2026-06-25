package com.example.loginapi.dto;

public class LoginResponse {

    private boolean success;
    private String perfil;
    private String nomeCompleto;
    private String dataNascimento;
    private String message;

    public LoginResponse(boolean success, String perfil, String nomeCompleto, String dataNascimento, String message) {
        this.success = success;
        this.perfil = perfil;
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public String getMessage() {
        return message;
    }
}