package com.example.loginapi.dto;

import java.util.List;

/**
 * DTO recebido pelo endpoint POST /api/mapa/rotas/importar.
 * Contém a identificação da rota e a lista ordenada de pontos.
 */
public class RotaLinhaImportRequest {

    private Long linhaId;
    private String sentido;
    private List<PontoDTO> pontos;

    public static class PontoDTO {
        private Double latitude;
        private Double longitude;

        public PontoDTO() {}
        public PontoDTO(Double latitude, Double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getSentido() { return sentido; }
    public void setSentido(String sentido) { this.sentido = sentido; }

    public List<PontoDTO> getPontos() { return pontos; }
    public void setPontos(List<PontoDTO> pontos) { this.pontos = pontos; }
}
