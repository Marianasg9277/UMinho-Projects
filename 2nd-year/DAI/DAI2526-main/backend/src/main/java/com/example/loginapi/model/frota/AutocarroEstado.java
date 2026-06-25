package com.example.loginapi.model.frota;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "autocarro_estado")
public class AutocarroEstado {

    @Id
    private Long autocarroId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "autocarro_id")
    private Autocarro autocarro;

    @Column(nullable = false, length = 30)
    private String estado = "ARMAZENADO";

    // Só preenchido quando estado = "EM_SERVICO". Null nos restantes estados.
    @Column(name = "sub_estado", length = 30)
    private String subEstado;

    // Momento em que o autocarro entrou em EM_SERVICO na viagem atual.
    // Null quando o estado não é EM_SERVICO.
    @Column(name = "inicio_servico")
    private LocalDateTime inicioServico;

    // Sentido em serviço: "IDA" ou "VOLTA". Null quando não está em EM_SERVICO.
    @Column(name = "sentido_atual", length = 10)
    private String sentidoAtual;

    // true = estado definido manualmente pelo admin; o script não sobrescreve o estado.
    // false = modo automático (script controla o estado).
    @Column(name = "controlo_manual", nullable = false, columnDefinition = "boolean default false")
    private boolean controloManual = false;

    @Column(nullable = false)
    private Integer ocupacao = 0;

    @Column(nullable = false)
    private Integer capacidade = 60;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getAutocarroId() { return autocarroId; }
    public Autocarro getAutocarro() { return autocarro; }
    public void setAutocarro(Autocarro autocarro) { this.autocarro = autocarro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getSubEstado() { return subEstado; }
    public void setSubEstado(String subEstado) { this.subEstado = subEstado; }
    public LocalDateTime getInicioServico() { return inicioServico; }
    public void setInicioServico(LocalDateTime inicioServico) { this.inicioServico = inicioServico; }
    public String getSentidoAtual() { return sentidoAtual; }
    public void setSentidoAtual(String sentidoAtual) { this.sentidoAtual = sentidoAtual; }
    public boolean isControloManual() { return controloManual; }
    public void setControloManual(boolean controloManual) { this.controloManual = controloManual; }
    public Integer getOcupacao() { return ocupacao; }
    public void setOcupacao(Integer ocupacao) { this.ocupacao = ocupacao; }
    public Integer getCapacidade() { return capacidade; }
    public void setCapacidade(Integer capacidade) { this.capacidade = capacidade; }
    public LocalDateTime getAtualizadoEm() { return atualizadoEm; }
}
