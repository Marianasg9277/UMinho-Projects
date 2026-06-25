package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;

/**
 * Zona/coroa tarifária (ex: Coroa 1, Coroa 2, Coroa 3).
 */
@Entity
@Table(name = "coroas")
public class Coroa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 200)
    private String descricao;

    /** Se esta coroa está disponível para seleção. */
    @Column(nullable = false)
    private boolean ativo = true;

    public Coroa() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
