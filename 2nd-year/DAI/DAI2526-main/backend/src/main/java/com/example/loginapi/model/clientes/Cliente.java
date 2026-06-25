package com.example.loginapi.model.clientes;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String perfil;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String sobrenome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String morada;

    @Column(nullable = false, unique = true, length = 9)
    private String nif;

    @Column(nullable = false)
    private String telefone;

    @Column(name = "numero_cartao_cidadao", nullable = false, unique = true)
    private String numeroCartaoCidadao;

    @Column(name = "digito_verificacao_cc", nullable = false)
    private String digitoVerificacaoCartaoCidadao;

    @OneToOne
    @JoinColumn(name = "utilizador_id", nullable = false, unique = true)
    private Utilizador utilizador;

    @Column(name = "foto_passe_path", length = 500)
    private String fotoPassePath;

    public Cliente() {}

    public Long getId() {
        return id;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

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

    public String getNomeCompleto() {
        return nome + " " + sobrenome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getMorada() {
        return morada;
    }

    public void setMorada(String morada) {
        this.morada = morada;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getNumeroCartaoCidadao() {
        return numeroCartaoCidadao;
    }

    public void setNumeroCartaoCidadao(String numeroCartaoCidadao) {
        this.numeroCartaoCidadao = numeroCartaoCidadao;
    }

    public String getDigitoVerificacaoCartaoCidadao() {
        return digitoVerificacaoCartaoCidadao;
    }

    public void setDigitoVerificacaoCartaoCidadao(String digitoVerificacaoCartaoCidadao) {
        this.digitoVerificacaoCartaoCidadao = digitoVerificacaoCartaoCidadao;
    }

    public Utilizador getUtilizador() {
        return utilizador;
    }

    public void setUtilizador(Utilizador utilizador) {
        this.utilizador = utilizador;
    }

    public String getFotoPassePath() {
        return fotoPassePath;
    }

    public void setFotoPassePath(String fotoPassePath) {
        this.fotoPassePath = fotoPassePath;
    }
}