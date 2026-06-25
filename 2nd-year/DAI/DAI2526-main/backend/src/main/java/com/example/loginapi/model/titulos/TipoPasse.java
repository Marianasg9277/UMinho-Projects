package com.example.loginapi.model.titulos;

import jakarta.persistence.*;

/**
 * Tipo de passe disponível para compra (ex: Mensal, Trimestral).
 */
@Entity
@Table(name = "tipos_passe")
public class TipoPasse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(length = 200)
    private String descricao;

    /** Duração do passe em dias (ex: 30 para mensal, 90 para trimestral). */
    @Column(name = "duracao_dias", nullable = false)
    private int duracaoDias;

    /** Se este tipo de passe está disponível para novas compras. */
    @Column(nullable = false)
    private boolean ativo = true;

    public TipoPasse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getDuracaoDias() { return duracaoDias; }
    public void setDuracaoDias(int duracaoDias) { this.duracaoDias = duracaoDias; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
