package com.example.loginapi.dto;

public class ParagemResponse {

    private Long id;
    private String nome;
    private Double latitude;
    private Double longitude;
    private String gtfsStopId;
    private String zoneId;
    private Boolean ativo;

    public ParagemResponse() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getGtfsStopId() { return gtfsStopId; }
    public void setGtfsStopId(String gtfsStopId) { this.gtfsStopId = gtfsStopId; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
