package com.example.loginapi.model.pagamentos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.example.loginapi.model.clientes.Cliente;


@Entity
@Table(name = "cartoes_pagamento")
public class CartaoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "nome_titular", nullable = false)
    private String nomeTitular;

    @Column(name = "ultimos4", nullable = false, length = 4)
    private String ultimos4Digitos;

    @Column(name = "bandeira", nullable = false, length = 20)
    private String bandeira;

    @Column(name = "mes_validade", nullable = false)
    private int mesValidade;

    @Column(name = "ano_validade", nullable = false)
    private int anoValidade;

    // Token simulado que representaria uma referência num gateway real
    @Column(name = "token_simulado", nullable = false, unique = true)
    private String tokenSimulado;

    @Column(name = "predefinido", nullable = false)
    private boolean predefinido = false;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void prePersist() {
        criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getUltimos4Digitos() { return ultimos4Digitos; }
    public void setUltimos4Digitos(String ultimos4Digitos) { this.ultimos4Digitos = ultimos4Digitos; }

    public String getBandeira() { return bandeira; }
    public void setBandeira(String bandeira) { this.bandeira = bandeira; }

    public int getMesValidade() { return mesValidade; }
    public void setMesValidade(int mesValidade) { this.mesValidade = mesValidade; }

    public int getAnoValidade() { return anoValidade; }
    public void setAnoValidade(int anoValidade) { this.anoValidade = anoValidade; }

    public String getTokenSimulado() { return tokenSimulado; }
    public void setTokenSimulado(String tokenSimulado) { this.tokenSimulado = tokenSimulado; }

    public boolean isPredefinido() { return predefinido; }
    public void setPredefinido(boolean predefinido) { this.predefinido = predefinido; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
