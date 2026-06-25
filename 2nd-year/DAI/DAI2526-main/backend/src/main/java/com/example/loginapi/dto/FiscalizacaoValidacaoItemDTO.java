package com.example.loginapi.dto;

import java.time.LocalDateTime;

public class FiscalizacaoValidacaoItemDTO {

    private LocalDateTime dataHora;
    private String linha;
    private Boolean sucesso;
    private String motivoRejeicao;
    private String detalhes;

    public FiscalizacaoValidacaoItemDTO() {}

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getLinha() { return linha; }
    public void setLinha(String linha) { this.linha = linha; }

    public Boolean getSucesso() { return sucesso; }
    public void setSucesso(Boolean sucesso) { this.sucesso = sucesso; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
}
