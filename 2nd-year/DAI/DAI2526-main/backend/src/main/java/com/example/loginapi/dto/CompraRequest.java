package com.example.loginapi.dto;

/**
 * Request body for POST /api/comprar
 */
public class CompraRequest {

    private Long tipoBilheteId;
    private Long linhaId;        // optional – null for passes not tied to a line
    private String guestEmail;   // optional – only needed for anonymous purchases

    public CompraRequest() {}

    public Long getTipoBilheteId() { return tipoBilheteId; }
    public void setTipoBilheteId(Long tipoBilheteId) { this.tipoBilheteId = tipoBilheteId; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
}
