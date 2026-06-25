package com.example.loginapi.dto;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Passe;


public class ClienteDadosResponse {

    private boolean success;
    private Long idCliente;
    private String perfil;
    private String role;
    private String nome;
    private String sobrenome;
    private String nomeCompleto;
    private String dataNascimento;
    private String morada;
    private String nif;
    private String telefone;
    private String numeroCartaoCidadao;
    private String message;
    private boolean temFotoPasse;
    private String fotoPasseUrl;

    public ClienteDadosResponse() {}

    public ClienteDadosResponse(Cliente cliente, String message) {
        this.success = true;
        this.idCliente = cliente.getId();
        this.perfil = cliente.getPerfil();
        this.role = cliente.getUtilizador() != null ? cliente.getUtilizador().getRole().name() : null;
        this.nome = cliente.getNome();
        this.sobrenome = cliente.getSobrenome();
        this.nomeCompleto = cliente.getNomeCompleto();
        this.dataNascimento = cliente.getDataNascimento() != null ? cliente.getDataNascimento().toString() : null;
        this.morada = cliente.getMorada();
        this.nif = cliente.getNif();
        this.telefone = cliente.getTelefone();
        this.numeroCartaoCidadao = cliente.getNumeroCartaoCidadao();
        this.message = message;
        this.temFotoPasse = cliente.getFotoPassePath() != null;
        this.fotoPasseUrl = cliente.getFotoPassePath() != null ? "/api/cliente/foto-passe" : null;
    }

    public boolean isSuccess() { return success; }
    public Long getIdCliente() { return idCliente; }
    public String getPerfil() { return perfil; }
    public String getRole() { return role; }
    public String getNome() { return nome; }
    public String getSobrenome() { return sobrenome; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getDataNascimento() { return dataNascimento; }
    public String getMorada() { return morada; }
    public String getNif() { return nif; }
    public String getTelefone() { return telefone; }
    public String getNumeroCartaoCidadao() { return numeroCartaoCidadao; }
    public String getMessage() { return message; }
    public boolean isTemFotoPasse() { return temFotoPasse; }
    public String getFotoPasseUrl() { return fotoPasseUrl; }
}
