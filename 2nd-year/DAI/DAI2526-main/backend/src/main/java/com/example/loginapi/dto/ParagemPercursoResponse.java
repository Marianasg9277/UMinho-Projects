package com.example.loginapi.dto;

public class ParagemPercursoResponse {

    private Integer ordem;
    private String nome;
    private Integer minutosDesdeInicio;
    private Double latitude;
    private Double longitude;

    public ParagemPercursoResponse() {}

    public ParagemPercursoResponse(Integer ordem, String nome, Integer minutosDesdeInicio,
                                   Double latitude, Double longitude) {
        this.ordem = ordem;
        this.nome = nome;
        this.minutosDesdeInicio = minutosDesdeInicio;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Integer getMinutosDesdeInicio() { return minutosDesdeInicio; }
    public void setMinutosDesdeInicio(Integer minutosDesdeInicio) { this.minutosDesdeInicio = minutosDesdeInicio; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
