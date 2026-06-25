package com.example.loginapi.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public class AutocarroPosicaoResponse {

    private String codigoAutocarro;
    private String nome;
    private Long linhaId;
    private String linhaNome;
    private Double latitude;
    private Double longitude;
    private Double velocidade;
    private Integer direcao;
    private LocalDateTime timestampReportado;
    private Instant recebidoEm;

    public AutocarroPosicaoResponse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getCodigoAutocarro() { return codigoAutocarro; }
    public void setCodigoAutocarro(String codigoAutocarro) { this.codigoAutocarro = codigoAutocarro; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getLinhaNome() { return linhaNome; }
    public void setLinhaNome(String linhaNome) { this.linhaNome = linhaNome; }

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

    public Instant getRecebidoEm() { return recebidoEm; }
    public void setRecebidoEm(Instant recebidoEm) { this.recebidoEm = recebidoEm; }

    // ── Campos novos ──────────────────────────────────────────────────────────
    private String estado;
    private String subEstado;
    private Integer ocupacao;
    private Integer capacidade;
    private Double percentagemOcupacao;   // ocupacao/capacidade * 100, arredondado a 1 casa decimal
    private String proximaParagemNome;    // null se velocidade = 0 ou não disponível
    private Integer etaMinutos;           // null se velocidade = 0 ou não disponível

    // Dados da viagem atual — preenchidos quando estado = EM_SERVICO; null caso contrário.
    private LocalDateTime inicioServico;
    private String sentidoAtual;

    // true = estado definido manualmente pelo admin (script não sobrescreve).
    private boolean controloManual;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSubEstado() { return subEstado; }
    public void setSubEstado(String subEstado) { this.subEstado = subEstado; }
    public Integer getOcupacao() { return ocupacao; }
    public void setOcupacao(Integer ocupacao) { this.ocupacao = ocupacao; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public Double getPercentagemOcupacao() { return percentagemOcupacao; }
    public void setPercentagemOcupacao(Double p) { this.percentagemOcupacao = p; }
    public String getProximaParagemNome() { return proximaParagemNome; }
    public void setProximaParagemNome(String n) { this.proximaParagemNome = n; }
    public Integer getEtaMinutos() { return etaMinutos; }
    public void setEtaMinutos(Integer etaMinutos) { this.etaMinutos = etaMinutos; }
    public LocalDateTime getInicioServico() { return inicioServico; }
    public void setInicioServico(LocalDateTime inicioServico) { this.inicioServico = inicioServico; }
    public String getSentidoAtual() { return sentidoAtual; }
    public void setSentidoAtual(String sentidoAtual) { this.sentidoAtual = sentidoAtual; }
    public boolean isControloManual() { return controloManual; }
    public void setControloManual(boolean controloManual) { this.controloManual = controloManual; }
}
