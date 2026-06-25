package com.example.loginapi.dto;

public class PasseQrAtualResponse {
    private String token;
    private String expiraEm;
    private String criadoEm;
    private String imagemBase64;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getExpiraEm() { return expiraEm; }
    public void setExpiraEm(String expiraEm) { this.expiraEm = expiraEm; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public String getImagemBase64() { return imagemBase64; }
    public void setImagemBase64(String imagemBase64) { this.imagemBase64 = imagemBase64; }
}
