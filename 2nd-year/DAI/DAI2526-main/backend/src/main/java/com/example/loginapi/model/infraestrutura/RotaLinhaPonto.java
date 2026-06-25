package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;
import com.example.loginapi.model.frota.Autocarro;


/**
 * Representa um ponto geográfico da rota de uma linha de autocarro.
 * Cada registo guarda latitude/longitude e a sua ordem no percurso.
 */
@Entity
@Table(name = "rota_linha_ponto",
       indexes = @Index(name = "idx_rota_linha_sentido", columnList = "linha_id, sentido"))
public class RotaLinhaPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID interno da linha de autocarro. */
    @Column(name = "linha_id", nullable = false)
    private Long linhaId;

    /** Sentido do percurso: "IDA" ou "VOLTA" */
    @Column(nullable = false, length = 10)
    private String sentido;

    /** Posição sequencial deste ponto na rota (começando em 0) */
    @Column(nullable = false)
    private Integer ordem;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    /** shape_id real do GTFS usado para esta rota. */
    @Column(name = "shape_id", length = 80)
    private String shapeId;

    public RotaLinhaPonto() {}

    public RotaLinhaPonto(Long linhaId, String sentido, Integer ordem, Double latitude, Double longitude) {
        this(linhaId, sentido, ordem, latitude, longitude, null);
    }

    public RotaLinhaPonto(Long linhaId, String sentido, Integer ordem, Double latitude, Double longitude, String shapeId) {
        this.linhaId = linhaId;
        this.sentido = sentido;
        this.ordem = ordem;
        this.latitude = latitude;
        this.longitude = longitude;
        this.shapeId = shapeId;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getShapeId() { return shapeId; }
    public void setShapeId(String shapeId) { this.shapeId = shapeId; }
}
