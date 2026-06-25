package com.example.loginapi.model.frota;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;
import com.example.loginapi.model.infraestrutura.Linha;


@Entity
@Table(name = "autocarro_ultima_posicao")
public class AutocarroUltimaPosicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Relação 1-para-1 com Autocarro (cada autocarro tem no máximo uma posição) */
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "autocarro_id", nullable = false, unique = true)
    private Autocarro autocarro;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** Velocidade em km/h, opcional */
    private Double velocidade;

    /** Direção em graus (0-359), opcional */
    private Integer direcao;

    /** Timestamp reportado pelo script Python */
    @Column(name = "timestamp_reportado", nullable = false)
    private LocalDateTime timestampReportado;

    /** Momento em que o backend recebeu a posição */
    @Column(name = "recebido_em", nullable = false)
    private Instant recebidoEm;

    public AutocarroUltimaPosicao() {}

    @PrePersist
    protected void onCreate() {
        if (this.recebidoEm == null) {
            this.recebidoEm = Instant.now();
        }
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Autocarro getAutocarro() { return autocarro; }
    public void setAutocarro(Autocarro autocarro) { this.autocarro = autocarro; }

    public Linha getLinha() { return linha; }
    public void setLinha(Linha linha) { this.linha = linha; }

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
}
