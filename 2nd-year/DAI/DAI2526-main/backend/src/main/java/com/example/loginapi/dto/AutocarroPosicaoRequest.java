package com.example.loginapi.dto;

import java.time.LocalDateTime;
import com.example.loginapi.model.frota.Autocarro;


public class AutocarroPosicaoRequest {

    private String codigoAutocarro;
    private String nome;
    private Long linhaId;
    private Double latitude;
    private Double longitude;
    private Double velocidade;
    private Integer direcao;
    private LocalDateTime timestampReportado;

    public AutocarroPosicaoRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getCodigoAutocarro() { return codigoAutocarro; }
    public void setCodigoAutocarro(String codigoAutocarro) { this.codigoAutocarro = codigoAutocarro; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getVelocidade() { return velocidade; }
    public void setVelocidade(Double velocidade) { this.velocidade = velocidade; }

    public Integer getDirecao() { return direcao; }
    public void setDirecao(Integer direcao) { this.direcao = direcao; }

    public LocalDateTime getTimestampReportado() { return timestampReportado; }
    public void setTimestampReportado(LocalDateTime timestampReportado) { this.timestampReportado = timestampReportado; }

    // ── Campos novos enviados pelo script ─────────────────────────────────────
    private String estado;      // "ARMAZENADO" | "EM_TRANSITO" | "EM_SERVICO" | "AVARIADO"
    private String subEstado;   // "PONTUAL" | "ATRASADO" | "ADIANTADO" — só quando EM_SERVICO
    private Integer ocupacao;   // número de passageiros a bordo
    private Integer capacidade; // capacidade máxima do autocarro

    // Momento em que o autocarro entrou em EM_SERVICO nesta viagem.
    // Enviado pelo script quando estado = EM_SERVICO; null caso contrário.
    private LocalDateTime inicioServico;

    // Sentido em serviço: "IDA" ou "VOLTA".
    private String sentido;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSubEstado() { return subEstado; }
    public void setSubEstado(String subEstado) { this.subEstado = subEstado; }
    public Integer getOcupacao() { return ocupacao; }
    public void setOcupacao(Integer ocupacao) { this.ocupacao = ocupacao; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public LocalDateTime getInicioServico() { return inicioServico; }
    public void setInicioServico(LocalDateTime inicioServico) { this.inicioServico = inicioServico; }
    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }
}
