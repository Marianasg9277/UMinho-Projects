package com.example.loginapi.dto;

public class LoginResponse {

    private boolean success;
    private Long idCliente;
    private String perfil;
    private String role;
    private String nomeCompleto;
    private String dataNascimento;
    private String morada;
    private String nif;
    private String telefone;
    private String numeroCartaoCidadao;
    private String message;

    public LoginResponse(boolean success, Long idCliente, String perfil, String role, String nomeCompleto, String message) {
        this.success = success;
        this.idCliente = idCliente;
        this.perfil = perfil;
        this.role = role;
        this.nomeCompleto = nomeCompleto;
        this.message = message;
    }

    public LoginResponse(boolean success, String perfil, String role, String nomeCompleto, String message) {
        this(success, null, perfil, role, nomeCompleto, message);
    }

    public LoginResponse(boolean success, Long idCliente, String perfil, String role, String nomeCompleto,
                         String dataNascimento, String morada, String nif, String telefone,
                         String numeroCartaoCidadao, String message) {
        this.success = success;
        this.idCliente = idCliente;
        this.perfil = perfil;
        this.role = role;
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.morada = morada;
        this.nif = nif;
        this.telefone = telefone;
        this.numeroCartaoCidadao = numeroCartaoCidadao;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public Long getIdCliente() { return idCliente; }
    public String getPerfil() { return perfil; }
    public String getRole() { return role; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getDataNascimento() { return dataNascimento; }
    public String getMorada() { return morada; }
    public String getNif() { return nif; }
    public String getTelefone() { return telefone; }
    public String getNumeroCartaoCidadao() { return numeroCartaoCidadao; }
    public String getMessage() { return message; }
}
