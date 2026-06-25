package com.example.loginapi.dto;

import java.time.LocalDateTime;
import com.example.loginapi.model.infraestrutura.Linha;


public class HistoricoValidacaoDTO {

    private Long id;
    private LocalDateTime dataValidacao;
    private String tipoTitulo;
    private String tipoDescricao;
    private String linha;
    private String detalhes;

    public HistoricoValidacaoDTO() {}

    public HistoricoValidacaoDTO(Long id, LocalDateTime dataValidacao, String tipoTitulo,
                                 String tipoDescricao, String linha, String detalhes) {
        this.id = id;
        this.dataValidacao = dataValidacao;
        this.tipoTitulo = tipoTitulo;
        this.tipoDescricao = tipoDescricao;
        this.linha = linha;
        this.detalhes = detalhes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }
    public String getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(String tipoTitulo) { this.tipoTitulo = tipoTitulo; }
    public String getTipoDescricao() { return tipoDescricao; }
    public void setTipoDescricao(String tipoDescricao) { this.tipoDescricao = tipoDescricao; }
    public String getLinha() { return linha; }
    public void setLinha(String linha) { this.linha = linha; }
    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }
}
