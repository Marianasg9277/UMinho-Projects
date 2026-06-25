package com.example.loginapi.model.comunicacao;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "avisos")
public class Aviso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AvisoTipo tipo;

    @Column(nullable = false)
    private boolean novo;

    public Aviso() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public AvisoTipo getTipo() { return tipo; }
    public void setTipo(AvisoTipo tipo) { this.tipo = tipo; }

    public boolean isNovo() { return novo; }
    public void setNovo(boolean novo) { this.novo = novo; }
}
