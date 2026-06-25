package com.example.loginapi.model.comunicacao;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    public enum Tipo { INFO, SUCESSO, AVISO, ERRO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 1000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tipo tipo;

    @Column(nullable = false)
    private boolean lida = false;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    /**
     * Email of the destination user.
     * Null = broadcast / admin notification (visible to all admins).
     */
    @Column(name = "utilizador_destino", length = 200)
    private String utilizadorDestino;

    @Column(nullable = false)
    private boolean ativa = true;

    public Notificacao() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getUtilizadorDestino() { return utilizadorDestino; }
    public void setUtilizadorDestino(String utilizadorDestino) { this.utilizadorDestino = utilizadorDestino; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
