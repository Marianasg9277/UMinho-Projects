package com.example.loginapi.model.titulos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.infraestrutura.Linha;


@Entity
@Table(name = "historico_validacoes", indexes = {
    @Index(name = "idx_cliente_data", columnList = "cliente_id,data_validacao")
})
public class HistoricoValidacao {

    public enum TipoTitulo {
        PASSE, BILHETE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "data_validacao", nullable = false)
    private LocalDateTime dataValidacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_titulo", nullable = false, length = 20)
    private TipoTitulo tipoTitulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transacao_id")
    private Transacao transacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passe_id")
    private Passe passe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linha_id")
    private Linha linha;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "coroa_id")
    private Coroa coroa;

    /** null = registo legado (anterior à implementação); true = validação bem-sucedida; false = tentativa inválida. */
    @Column(name = "sucesso")
    private Boolean sucesso;

    @Column(name = "motivo_rejeicao", length = 60)
    private String motivoRejeicao;

    @Column(name = "tipo_descricao", length = 100)
    private String tipoDescricao;

    @Column(name = "detalhes", length = 200)
    private String detalhes;

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }
    public TipoTitulo getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(TipoTitulo tipoTitulo) { this.tipoTitulo = tipoTitulo; }
    public Transacao getTransacao() { return transacao; }
    public void setTransacao(Transacao transacao) { this.transacao = transacao; }
    public Passe getPasse() { return passe; }
    public void setPasse(Passe passe) { this.passe = passe; }
    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }
    public Coroa getCoroa() { return coroa; }
    public void setCoroa(Coroa coroa) { this.coroa = coroa; }
    public Boolean getSucesso() { return sucesso; }
    public void setSucesso(Boolean sucesso) { this.sucesso = sucesso; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    public String getTipoDescricao() { return tipoDescricao; }
    public void setTipoDescricao(String tipoDescricao) { this.tipoDescricao = tipoDescricao; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
}
