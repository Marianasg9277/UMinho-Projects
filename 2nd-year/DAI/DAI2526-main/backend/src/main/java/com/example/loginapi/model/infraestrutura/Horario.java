package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;

@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    /** Short stop name, e.g. "Largo do Prado" */
    @Column(nullable = false)
    private String paragem;

    /** Minutes until arrival – used as "real-time" value */
    @Column(name = "minutos_ate", nullable = false)
    private int minutosAte;

    public Horario() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }

    public String getParagem() { return paragem; }
    public void setParagem(String paragem) { this.paragem = paragem; }

    public int getMinutosAte() { return minutosAte; }
    public void setMinutosAte(int minutosAte) { this.minutosAte = minutosAte; }
}
