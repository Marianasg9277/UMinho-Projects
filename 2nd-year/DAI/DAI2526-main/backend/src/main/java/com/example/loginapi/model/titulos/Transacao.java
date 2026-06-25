package com.example.loginapi.model.titulos;

import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.infraestrutura.Linha;


@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "guest_email")
    private String guestEmail;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_bilhete_id", nullable = false)
    private TipoBilhete tipoBilhete;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linha_id")
    private Linha linha;

    @Column(name = "data_compra", nullable = false)
    private LocalDateTime dataCompra;

    @Column(name = "preco", precision = 8, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pagamento", length = 20)
    private EstadoPagamento estadoPagamento;

    @Column(name = "codigo_qr", unique = true, length = 120)
    private String codigoQr;

    @Column(name = "valido_ate")
    private LocalDateTime validoAte;

    @Column(name = "guest_nome", length = 120)
    private String guestNome;

    @Column(name = "guest_nif", length = 9)
    private String guestNif;

    /** Coroa selecionada (para bilhetes sem linha mas com zona tarifária). */
    @Column(name = "coroa_id")
    private Long coroaId;

    /** Momento da primeira validação bem-sucedida — null se ainda não validado. A validade temporal começa aqui. */
    @Column(name = "primeira_validacao_em")
    private LocalDateTime primeiraValidacaoEm;

    @Column(name = "fatura_numero", length = 60)
    private String faturaNumero;

    @Column(name = "fatura_emitida_em")
    private LocalDateTime faturaEmitidaEm;

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public TipoBilhete getTipoBilhete() { return tipoBilhete; }
    public void setTipoBilhete(TipoBilhete tipoBilhete) { this.tipoBilhete = tipoBilhete; }
    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }
    public LocalDateTime getDataCompra() { return dataCompra; }
    public void setDataCompra(LocalDateTime dataCompra) { this.dataCompra = dataCompra; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public EstadoPagamento getEstadoPagamento() { return estadoPagamento; }
    public void setEstadoPagamento(EstadoPagamento estadoPagamento) { this.estadoPagamento = estadoPagamento; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public LocalDateTime getValidoAte() { return validoAte; }
    public void setValidoAte(LocalDateTime validoAte) { this.validoAte = validoAte; }
    public String getGuestNome() { return guestNome; }
    public void setGuestNome(String guestNome) { this.guestNome = guestNome; }
    public String getGuestNif() { return guestNif; }
    public void setGuestNif(String guestNif) { this.guestNif = guestNif; }
    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }
    public LocalDateTime getPrimeiraValidacaoEm() { return primeiraValidacaoEm; }
    public void setPrimeiraValidacaoEm(LocalDateTime primeiraValidacaoEm) { this.primeiraValidacaoEm = primeiraValidacaoEm; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
    public LocalDateTime getFaturaEmitidaEm() { return faturaEmitidaEm; }
    public void setFaturaEmitidaEm(LocalDateTime faturaEmitidaEm) { this.faturaEmitidaEm = faturaEmitidaEm; }
}
