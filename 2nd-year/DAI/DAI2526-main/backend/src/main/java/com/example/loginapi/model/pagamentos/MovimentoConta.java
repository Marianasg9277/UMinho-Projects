package com.example.loginapi.model.pagamentos;

import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "movimentos_conta")
public class MovimentoConta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimentoConta tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_antes", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoAntes;

    @Column(name = "saldo_depois", nullable = false, precision = 10, scale = 2)
    private BigDecimal saldoDepois;

    @Column(length = 255)
    private String descricao;

    // Referência opcional ao pagamento que originou o movimento
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "pagamento_id")
    private Pagamento pagamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public MovimentoConta() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }

    public Conta getConta() { return conta; }
    public void setConta(Conta conta) { this.conta = conta; }

    public TipoMovimentoConta getTipo() { return tipo; }
    public void setTipo(TipoMovimentoConta tipo) { this.tipo = tipo; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getSaldoAntes() { return saldoAntes; }
    public void setSaldoAntes(BigDecimal saldoAntes) { this.saldoAntes = saldoAntes; }

    public BigDecimal getSaldoDepois() { return saldoDepois; }
    public void setSaldoDepois(BigDecimal saldoDepois) { this.saldoDepois = saldoDepois; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
