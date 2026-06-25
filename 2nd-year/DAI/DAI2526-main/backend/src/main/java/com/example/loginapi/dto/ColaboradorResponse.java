package com.example.loginapi.dto;

import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;

import java.time.LocalDate;

public class ColaboradorResponse {

    private Long id;
    private String nome;
    private String email;
    private String morada;
    private LocalDate dataNascimento;
    private String nif;
    private TipoColaborador tipoColaborador;
    private String numeroCarta;
    private Boolean ativo;
    /** Preenchido apenas na criação com password gerada automaticamente. Nunca persiste. */
    private String passwordTemporaria;

    // Campos de alocação (null quando não alocado)
    private Long autocarroId;
    private String autocarroCodigo;
    private Long linhaAtualId;
    private String linhaAtualNome;

    public ColaboradorResponse() {}

    public static ColaboradorResponse from(Colaborador c) {
        ColaboradorResponse r = new ColaboradorResponse();
        r.id              = c.getId();
        r.nome            = c.getNome();
        r.email           = c.getEmail();
        r.morada          = c.getMorada();
        r.dataNascimento  = c.getDataNascimento();
        r.nif             = c.getNif();
        r.tipoColaborador = c.getTipoColaborador();
        r.numeroCarta     = c.getNumeroCarta();
        r.ativo           = c.getAtivo();
        if (c.getAutocarro() != null) {
            r.autocarroId     = c.getAutocarro().getId();
            r.autocarroCodigo = c.getAutocarro().getCodigo();
        }
        if (c.getLinhaAtual() != null) {
            r.linhaAtualId   = c.getLinhaAtual().getId();
            r.linhaAtualNome = "Linha " + c.getLinhaAtual().getNumero() + " — " + c.getLinhaAtual().getNome();
        }
        return r;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getMorada() { return morada; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getNif() { return nif; }
    public TipoColaborador getTipoColaborador() { return tipoColaborador; }
    public String getNumeroCarta() { return numeroCarta; }
    public Boolean getAtivo() { return ativo; }

    public String getPasswordTemporaria() { return passwordTemporaria; }
    public void setPasswordTemporaria(String passwordTemporaria) { this.passwordTemporaria = passwordTemporaria; }

    public Long getAutocarroId() { return autocarroId; }
    public String getAutocarroCodigo() { return autocarroCodigo; }
    public Long getLinhaAtualId() { return linhaAtualId; }
    public String getLinhaAtualNome() { return linhaAtualNome; }
}
