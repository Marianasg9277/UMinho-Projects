package com.example.loginapi.model.clientes;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Documento (comprovativo) anexo a um pedido de estatuto.
 * O ficheiro físico é guardado no filesystem; aqui ficam os metadados.
 */
@Entity
@Table(name = "documentos_estatuto")
public class DocumentoEstatuto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private PedidoEstatuto pedido;

    @Column(name = "nome_ficheiro", nullable = false)
    private String nomeFicheiro;

    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    /** Caminho relativo no filesystem de storage (ex: "estatutos/42/doc_001.pdf"). */
    @Column(name = "caminho_storage", nullable = false)
    private String caminhoStorage;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public DocumentoEstatuto() {}

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public PedidoEstatuto getPedido() { return pedido; }
    public void setPedido(PedidoEstatuto pedido) { this.pedido = pedido; }

    public String getNomeFicheiro() { return nomeFicheiro; }
    public void setNomeFicheiro(String nomeFicheiro) { this.nomeFicheiro = nomeFicheiro; }

    public String getTipoConteudo() { return tipoConteudo; }
    public void setTipoConteudo(String tipoConteudo) { this.tipoConteudo = tipoConteudo; }

    public long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public String getCaminhoStorage() { return caminhoStorage; }
    public void setCaminhoStorage(String caminhoStorage) { this.caminhoStorage = caminhoStorage; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
}
