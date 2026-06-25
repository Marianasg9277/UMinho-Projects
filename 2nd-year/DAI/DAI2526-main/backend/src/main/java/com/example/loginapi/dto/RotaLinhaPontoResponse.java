package com.example.loginapi.dto;

/**
 * DTO devolvido pelo endpoint GET /api/mapa/rotas/{linhaId}/{sentido}.
 * Cada objecto representa um ponto da rota, ordenado por 'ordem'.
 */
public class RotaLinhaPontoResponse {

    private Integer ordem;
    private Double latitude;
    private Double longitude;

    public RotaLinhaPontoResponse() {}

    public RotaLinhaPontoResponse(Integer ordem, Double latitude, Double longitude) {
        this.ordem = ordem;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
