package com.example.loginapi.dto;

import java.util.List;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;


public class PedidoEstatutoResponse {
    private Long id;
    private String tipoEstatuto;
    private String estado;
    private String observacoesCliente;
    private String observacoesRevisor;
    private String criadoEm;
    private String atualizadoEm;
    private List<DocumentoInfo> documentos;

    // ── Campos adicionais do cliente (usados pelo backoffice) ─────────────────
    private String nomeCliente;
    private String emailCliente;
    private String estatutoAtual;
    private String revisadoPor;

    public PedidoEstatutoResponse() {}

    public static class DocumentoInfo {
        private Long id;
        private String nomeFicheiro;
        private String tipoConteudo;
        private long tamanhoBytes;
        private String criadoEm;

        public DocumentoInfo() {}
        public DocumentoInfo(Long id, String nomeFicheiro, String tipoConteudo, long tamanhoBytes, String criadoEm) {
            this.id = id; this.nomeFicheiro = nomeFicheiro; this.tipoConteudo = tipoConteudo;
            this.tamanhoBytes = tamanhoBytes; this.criadoEm = criadoEm;
        }

        public Long getId() { return id; }
        public String getNomeFicheiro() { return nomeFicheiro; }
        public String getTipoConteudo() { return tipoConteudo; }
        public long getTamanhoBytes() { return tamanhoBytes; }
        public String getCriadoEm() { return criadoEm; }
    }

    // Getters & Setters base
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(String tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacoesCliente() { return observacoesCliente; }
    public void setObservacoesCliente(String observacoesCliente) { this.observacoesCliente = observacoesCliente; }
    public String getObservacoesRevisor() { return observacoesRevisor; }
    public void setObservacoesRevisor(String observacoesRevisor) { this.observacoesRevisor = observacoesRevisor; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public String getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(String atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public List<DocumentoInfo> getDocumentos() { return documentos; }
    public void setDocumentos(List<DocumentoInfo> documentos) { this.documentos = documentos; }

    // Getters & Setters — campos do cliente
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
    public String getEstatutoAtual() { return estatutoAtual; }
    public void setEstatutoAtual(String estatutoAtual) { this.estatutoAtual = estatutoAtual; }
    public String getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(String revisadoPor) { this.revisadoPor = revisadoPor; }
}
