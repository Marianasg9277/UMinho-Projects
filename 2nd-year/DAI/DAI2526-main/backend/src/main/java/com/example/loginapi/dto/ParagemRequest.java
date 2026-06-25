package com.example.loginapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParagemRequest {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    private Double latitude;

    private Double longitude;

    @Size(max = 50, message = "O gtfsStopId não pode exceder 50 caracteres")
    private String gtfsStopId;

    @Size(max = 20, message = "O zoneId não pode exceder 20 caracteres")
    private String zoneId;

    public ParagemRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

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
}
