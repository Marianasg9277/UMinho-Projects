package com.example.loginapi.model.titulos;

import java.math.BigDecimal;
import jakarta.persistence.*;
import com.example.loginapi.model.infraestrutura.Coroa;

@Entity
@Table(name = "tipos_bilhete")
public class TipoBilhete {

    public enum Categoria { AVULSO, MENSAL, ZAPPING }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador real do tarifário no GTFS (fare_attributes.txt: fare_id). */
    @Column(name = "gtfs_fare_id", unique = true, length = 80)
    private String gtfsFareId;

    @Column(nullable = false)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Categoria categoria;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal preco;

    /** Duração em horas da validade do bilhete após compra (ex: 2 para avulso, 24 para diário). */
    @Column(name = "validade_horas")
    private Integer validadeHoras = 2;

    /** Duração real em segundos indicada no GTFS (fare_attributes.txt: transfer_duration). */
    @Column(name = "transfer_duration")
    private Integer transferDuration;

    /** Número de transbordos indicado no GTFS (fare_attributes.txt: transfers). */
    @Column(name = "transfers")
    private Integer transfers;

    /** Coroa tarifária associada a este tipo de bilhete (null = sem restrição de zona). */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coroa_id")
    private Coroa coroa;

    /** Optional description, e.g. "app/cartão" */
    private String descricao;

    /** Soft delete — false = desativado, não aparece no preçário público */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;

    public TipoBilhete() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getGtfsFareId() { return gtfsFareId; }
    public void setGtfsFareId(String gtfsFareId) { this.gtfsFareId = gtfsFareId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }

    public Coroa getCoroa() { return coroa; }
    public void setCoroa(Coroa coroa) { this.coroa = coroa; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getValidadeHoras() { return validadeHoras != null ? validadeHoras : 2; }
    public void setValidadeHoras(Integer validadeHoras) { this.validadeHoras = validadeHoras; }

    public Integer getTransferDuration() { return transferDuration; }
    public void setTransferDuration(Integer transferDuration) { this.transferDuration = transferDuration; }

    public Integer getTransfers() { return transfers; }
    public void setTransfers(Integer transfers) { this.transfers = transfers; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
