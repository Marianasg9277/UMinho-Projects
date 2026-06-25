package com.example.loginapi.model.infraestrutura;

import jakarta.persistence.*;

@Entity
@Table(name = "paragens")
public class Paragem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador real da paragem no GTFS (stops.txt: stop_id). */
    @Column(name = "gtfs_stop_id", unique = true, length = 50)
    private String gtfsStopId;

    @Column(nullable = false)
    private String nome;

    private Double latitude;

    private Double longitude;

    /** Zona/coroa real indicada no GTFS (stops.txt: zone_id). */
    @Column(name = "zone_id", length = 20)
    private String zoneId;

    @Column(nullable = false)
    private Boolean ativo = true;

    public Paragem() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }

    public String getGtfsStopId() { return gtfsStopId; }
    public void setGtfsStopId(String gtfsStopId) { this.gtfsStopId = gtfsStopId; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
