package com.example.loginapi.model.infraestrutura;

import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.example.loginapi.model.titulos.TipoPasse;


/**
 * Regra de preço: define o preço para uma combinação
 * (tipo_estatuto + tipo_passe + coroa) com período de vigência.
 *
 * Permite versionamento de preços: basta criar nova regra com nova data.
 * A regra ativa é a mais recente cujo período contenha a data atual.
 */
@Entity
@Table(name = "regras_preco")
public class RegraPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_estatuto", nullable = false, length = 20)
    private TipoEstatuto tipoEstatuto;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_passe_id", nullable = false)
    private TipoPasse tipoPasse;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "coroa_id", nullable = false)
    private Coroa coroa;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    @Column(name = "data_inicio_vigencia", nullable = false)
    private LocalDate dataInicioVigencia;

    /** Null = sem data fim (vigência indefinida). */
    @Column(name = "data_fim_vigencia")
    private LocalDate dataFimVigencia;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public RegraPreco() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    /**
     * Verifica se esta regra é válida para uma data específica.
     */
    public boolean isVigenteEm(LocalDate data) {
        if (!ativo) return false;
        if (data.isBefore(dataInicioVigencia)) return false;
        if (dataFimVigencia != null && data.isAfter(dataFimVigencia)) return false;
        return true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public TipoEstatuto getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(TipoEstatuto tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }

    public TipoPasse getTipoPasse() { return tipoPasse; }
    public void setTipoPasse(TipoPasse tipoPasse) { this.tipoPasse = tipoPasse; }

    public Coroa getCoroa() { return coroa; }
    public void setCoroa(Coroa coroa) { this.coroa = coroa; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public LocalDate getDataInicioVigencia() { return dataInicioVigencia; }
    public void setDataInicioVigencia(LocalDate dataInicioVigencia) { this.dataInicioVigencia = dataInicioVigencia; }

    public LocalDate getDataFimVigencia() { return dataFimVigencia; }
    public void setDataFimVigencia(LocalDate dataFimVigencia) { this.dataFimVigencia = dataFimVigencia; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
