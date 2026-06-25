package com.example.loginapi.model.clientes;

import com.example.loginapi.model.clientes.enums.EstadoEstatutoUtilizador;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Estatuto ativo (ou histórico) de um utilizador.
 * Cada utilizador deve ter no máximo um estatuto ACTIVE de cada vez.
 */
@Entity
@Table(name = "estatutos_utilizador")
public class EstatutoUtilizador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_estatuto", nullable = false, length = 20)
    private TipoEstatuto tipoEstatuto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEstatutoUtilizador estado;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    /** Null para estatutos sem data de expiração (ex: INCAPACITADO, derivados de idade). */
    @Column(name = "data_fim")
    private LocalDate dataFim;

    /** Pedido que originou este estatuto. Null para estatutos automáticos sem pedido explícito. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private PedidoEstatuto pedido;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public EstatutoUtilizador() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public TipoEstatuto getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(TipoEstatuto tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }

    public EstadoEstatutoUtilizador getEstado() { return estado; }
    public void setEstado(EstadoEstatutoUtilizador estado) { this.estado = estado; }

    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }

    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }

    public PedidoEstatuto getPedido() { return pedido; }
    public void setPedido(PedidoEstatuto pedido) { this.pedido = pedido; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
