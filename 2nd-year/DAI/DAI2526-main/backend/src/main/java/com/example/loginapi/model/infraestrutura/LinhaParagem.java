package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;

@Entity
@Table(name = "linha_paragens")
public class LinhaParagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "paragem_id", nullable = false)
    private Paragem paragem;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "minutos_desde_inicio", nullable = false)
    private Integer minutosDesdeInicio;

    /** Sentido GTFS simplificado: IDA para direction_id=0 e VOLTA para direction_id=1. */
    @Column(length = 20)
    private String sentido;

    public LinhaParagem() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }

    public Paragem getParagem() { return paragem; }
    public void setParagem(Paragem paragem) { this.paragem = paragem; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Integer getMinutosDesdeInicio() { return minutosDesdeInicio; }
    public void setMinutosDesdeInicio(Integer minutosDesdeInicio) { this.minutosDesdeInicio = minutosDesdeInicio; }

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
}
