package com.example.loginapi.model.pagamentos;

import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.loginapi.model.titulos.Passe;


@Entity
@Table(name = "pagamentos")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "passe_id", nullable = false)
    private Passe passe;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPagamento estado;

    @Column(length = 30)
    private String metodo;

    @Column(name = "referencia_externa", length = 100)
    private String referenciaExterna;

    @Column(name = "fatura_numero", length = 60)
    private String faturaNumero;

    @Column(name = "fatura_emitida_em")
    private LocalDateTime faturaEmitidaEm;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public Pagamento() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Passe getPasse() { return passe; }
    public void setPasse(Passe passe) { this.passe = passe; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public EstadoPagamento getEstado() { return estado; }
    public void setEstado(EstadoPagamento estado) { this.estado = estado; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public void setReferenciaExterna(String referenciaExterna) { this.referenciaExterna = referenciaExterna; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
    public LocalDateTime getFaturaEmitidaEm() { return faturaEmitidaEm; }
    public void setFaturaEmitidaEm(LocalDateTime faturaEmitidaEm) { this.faturaEmitidaEm = faturaEmitidaEm; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
