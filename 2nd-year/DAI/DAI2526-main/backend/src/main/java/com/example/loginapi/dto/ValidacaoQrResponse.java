package com.example.loginapi.dto;

import com.example.loginapi.model.titulos.Passe;


public class ValidacaoQrResponse {
    private boolean valido;
    private String tipoDocumento; // "PASSE" | "BILHETE"
    private String estadoOperacional;
    private String tipoPasseNome; // nome do passe ou bilhete
    private String coroaNome;
    private String dataInicio;
    private String dataFim;
    private String mensagem;
    private String nomeTitular;
    private String motivoRejeicao; // TOKEN_EXPIRADO | TOKEN_REVOGADO | TOKEN_NAO_ENCONTRADO | PASSE_EXPIRADO | PASSE_NAO_INICIADO | PASSE_INATIVO | PASSE_INVALIDO | BILHETE_NAO_PAGO | BILHETE_JA_USADO | BILHETE_EXPIRADO
    private boolean temFotoPasse;
    private String fotoTitularUrl;
    private String linhaAlocada;
    private String coroaValidada;
    private boolean requiresLinhaManual;

    public ValidacaoQrResponse() {}

    public ValidacaoQrResponse(boolean valido, String mensagem) {
        this.valido = valido;
        this.mensagem = mensagem;
    }

    public ValidacaoQrResponse(boolean valido, String motivoRejeicao, String mensagem) {
        this.valido = valido;
        this.motivoRejeicao = motivoRejeicao;
        this.mensagem = mensagem;
    }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getEstadoOperacional() { return estadoOperacional; }
    public void setEstadoOperacional(String estadoOperacional) { this.estadoOperacional = estadoOperacional; }
    public String getTipoPasseNome() { return tipoPasseNome; }
    public void setTipoPasseNome(String tipoPasseNome) { this.tipoPasseNome = tipoPasseNome; }
    public String getCoroaNome() { return coroaNome; }
    public void setCoroaNome(String coroaNome) { this.coroaNome = coroaNome; }
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }
    public boolean isTemFotoPasse() { return temFotoPasse; }
    public void setTemFotoPasse(boolean temFotoPasse) { this.temFotoPasse = temFotoPasse; }
    public String getFotoTitularUrl() { return fotoTitularUrl; }
    public void setFotoTitularUrl(String fotoTitularUrl) { this.fotoTitularUrl = fotoTitularUrl; }
    public String getLinhaAlocada() { return linhaAlocada; }
    public void setLinhaAlocada(String linhaAlocada) { this.linhaAlocada = linhaAlocada; }
    public String getCoroaValidada() { return coroaValidada; }
    public void setCoroaValidada(String coroaValidada) { this.coroaValidada = coroaValidada; }
    public boolean isRequiresLinhaManual() { return requiresLinhaManual; }
    public void setRequiresLinhaManual(boolean requiresLinhaManual) { this.requiresLinhaManual = requiresLinhaManual; }
}
