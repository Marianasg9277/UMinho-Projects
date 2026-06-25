package com.example.loginapi.dto;

public class CompraResponse {

    private boolean success;
    private String message;
    private String codigoQr;
    private Long transacaoId;

    public CompraResponse(boolean success, String message, String codigoQr, Long transacaoId) {
        this.success = success;
        this.message = message;
        this.codigoQr = codigoQr;
        this.transacaoId = transacaoId;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getCodigoQr() { return codigoQr; }
    public Long getTransacaoId() { return transacaoId; }
}
