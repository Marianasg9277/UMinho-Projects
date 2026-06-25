package com.example.loginapi.dto;

import java.time.LocalDateTime;
import java.util.List;

public class FiscalizacaoVerificacaoResponse {

    private boolean valido;
    private String mensagem;
    private String motivoInvalidade;

    private String tipoTitulo;
    private String titular;
    private String tipoPasse;
    private String tipoBilhete;
    private String coroa;
    private String estatuto;
    private String validadeInicio;
    private String validadeFim;
    private String estado;

    private boolean temFotoPasse;
    private String fotoTitularUrl;

    private String linhaFiscalizador;
    private String linhaValidacao;
    private LocalDateTime dataHoraValidacao;
    private Boolean validadoNaLinhaAtual;

    private List<FiscalizacaoValidacaoItemDTO> ultimasValidacoes;

    public FiscalizacaoVerificacaoResponse() {}

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getMotivoInvalidade() { return motivoInvalidade; }
    public void setMotivoInvalidade(String motivoInvalidade) { this.motivoInvalidade = motivoInvalidade; }

    public String getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(String tipoTitulo) { this.tipoTitulo = tipoTitulo; }

    public String getTitular() { return titular; }
    public void setTitular(String titular) { this.titular = titular; }

    public String getTipoPasse() { return tipoPasse; }
    public void setTipoPasse(String tipoPasse) { this.tipoPasse = tipoPasse; }

    public String getTipoBilhete() { return tipoBilhete; }
    public void setTipoBilhete(String tipoBilhete) { this.tipoBilhete = tipoBilhete; }

    public String getCoroa() { return coroa; }
    public void setCoroa(String coroa) { this.coroa = coroa; }

    public String getEstatuto() { return estatuto; }
    public void setEstatuto(String estatuto) { this.estatuto = estatuto; }

    public String getValidadeInicio() { return validadeInicio; }
    public void setValidadeInicio(String validadeInicio) { this.validadeInicio = validadeInicio; }

    public String getValidadeFim() { return validadeFim; }
    public void setValidadeFim(String validadeFim) { this.validadeFim = validadeFim; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public boolean isTemFotoPasse() { return temFotoPasse; }
    public void setTemFotoPasse(boolean temFotoPasse) { this.temFotoPasse = temFotoPasse; }

    public String getFotoTitularUrl() { return fotoTitularUrl; }
    public void setFotoTitularUrl(String fotoTitularUrl) { this.fotoTitularUrl = fotoTitularUrl; }

    public String getLinhaFiscalizador() { return linhaFiscalizador; }
    public void setLinhaFiscalizador(String linhaFiscalizador) { this.linhaFiscalizador = linhaFiscalizador; }

    public String getLinhaValidacao() { return linhaValidacao; }
    public void setLinhaValidacao(String linhaValidacao) { this.linhaValidacao = linhaValidacao; }

    public LocalDateTime getDataHoraValidacao() { return dataHoraValidacao; }
    public void setDataHoraValidacao(LocalDateTime dataHoraValidacao) { this.dataHoraValidacao = dataHoraValidacao; }

    public Boolean getValidadoNaLinhaAtual() { return validadoNaLinhaAtual; }
    public void setValidadoNaLinhaAtual(Boolean validadoNaLinhaAtual) { this.validadoNaLinhaAtual = validadoNaLinhaAtual; }

    public List<FiscalizacaoValidacaoItemDTO> getUltimasValidacoes() { return ultimasValidacoes; }
    public void setUltimasValidacoes(List<FiscalizacaoValidacaoItemDTO> ultimasValidacoes) { this.ultimasValidacoes = ultimasValidacoes; }
}
