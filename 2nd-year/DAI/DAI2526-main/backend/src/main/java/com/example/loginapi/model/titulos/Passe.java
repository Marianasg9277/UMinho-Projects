package com.example.loginapi.model.titulos;

import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.infraestrutura.RegraPreco;


@Entity
@Table(name = "passes")
public class Passe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_passe_id", nullable = false)
    private TipoPasse tipoPasse;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coroa_id", nullable = false)
    private Coroa coroa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_estatuto_aplicado", nullable = false, length = 20)
    private TipoEstatuto tipoEstatutoAplicado;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "regra_preco_id")
    private RegraPreco regraPreco;

    @Column(name = "preco_aplicado", nullable = false, precision = 8, scale = 2)
    private BigDecimal precoAplicado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_comercial", nullable = false, length = 20)
    private EstadoComercialPasse estadoComercial;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operacional", nullable = false, length = 20)
    private EstadoOperacionalPasse estadoOperacional;

    @Column(name = "codigo_qr", nullable = false, unique = true, length = 80)
    private String codigoQr;

    // Legacy QR fields — kept for DB compatibility. QR tokens are now managed in PasseQrToken.
    @Column(name = "qr_token_atual", unique = true, length = 120)
    private String qrTokenAtual;

    @Column(name = "qr_gerado_em")
    private LocalDateTime qrGeradoEm;

    @Column(name = "qr_expira_em")
    private LocalDateTime qrExpiraEm;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public Passe() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now(ZoneOffset.UTC);
        atualizadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public TipoPasse getTipoPasse() { return tipoPasse; }
    public void setTipoPasse(TipoPasse tipoPasse) { this.tipoPasse = tipoPasse; }
    public Coroa getCoroa() { return coroa; }
    public void setCoroa(Coroa coroa) { this.coroa = coroa; }
    public TipoEstatuto getTipoEstatutoAplicado() { return tipoEstatutoAplicado; }
    public void setTipoEstatutoAplicado(TipoEstatuto tipoEstatutoAplicado) { this.tipoEstatutoAplicado = tipoEstatutoAplicado; }
    public RegraPreco getRegraPreco() { return regraPreco; }
    public void setRegraPreco(RegraPreco regraPreco) { this.regraPreco = regraPreco; }
    public BigDecimal getPrecoAplicado() { return precoAplicado; }
    public void setPrecoAplicado(BigDecimal precoAplicado) { this.precoAplicado = precoAplicado; }
    public EstadoComercialPasse getEstadoComercial() { return estadoComercial; }
    public void setEstadoComercial(EstadoComercialPasse estadoComercial) { this.estadoComercial = estadoComercial; }
    public EstadoOperacionalPasse getEstadoOperacional() { return estadoOperacional; }
    public void setEstadoOperacional(EstadoOperacionalPasse estadoOperacional) { this.estadoOperacional = estadoOperacional; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public String getQrTokenAtual() { return qrTokenAtual; }
    public void setQrTokenAtual(String qrTokenAtual) { this.qrTokenAtual = qrTokenAtual; }
    public LocalDateTime getQrGeradoEm() { return qrGeradoEm; }
    public void setQrGeradoEm(LocalDateTime qrGeradoEm) { this.qrGeradoEm = qrGeradoEm; }
    public LocalDateTime getQrExpiraEm() { return qrExpiraEm; }
    public void setQrExpiraEm(LocalDateTime qrExpiraEm) { this.qrExpiraEm = qrExpiraEm; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
