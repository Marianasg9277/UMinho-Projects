package com.example.loginapi.model.clientes;

import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pedido de atribuição de estatuto submetido por um utilizador.
 * Segue um workflow: DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED/REJECTED.
 */
@Entity
@Table(name = "pedidos_estatuto")
public class PedidoEstatuto {

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
    @Column(nullable = false, length = 25)
    private EstadoPedidoEstatuto estado;

    @Column(name = "observacoes_cliente", length = 1000)
    private String observacoesCliente;

    @Column(name = "observacoes_revisor", length = 1000)
    private String observacoesRevisor;

    @Column(name = "revisor_email", length = 200)
    private String revisorEmail;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoEstatuto> documentos = new ArrayList<>();

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public PedidoEstatuto() {}

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

    public EstadoPedidoEstatuto getEstado() { return estado; }
    public void setEstado(EstadoPedidoEstatuto estado) { this.estado = estado; }

    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }

    public String getObservacoesRevisor() { return observacoesRevisor; }
    public void setObservacoesRevisor(String observacoesRevisor) { this.observacoesRevisor = observacoesRevisor; }

    public String getRevisorEmail() { return revisorEmail; }
    public void setRevisorEmail(String revisorEmail) { this.revisorEmail = revisorEmail; }

    public List<DocumentoEstatuto> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentoEstatuto> documentos) { this.documentos = documentos; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
