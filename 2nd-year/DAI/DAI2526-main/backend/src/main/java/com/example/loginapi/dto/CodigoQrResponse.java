package com.example.loginapi.dto;

public class CodigoQrResponse {
    private Long id;
    private String tipo;
    private String token;
    private String payload;
    private String geradoEm;
    private String expiraEm;
    private long segundosRestantes;
    private String mensagem;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public String getGeradoEm() { return geradoEm; }
    public void setGeradoEm(String geradoEm) { this.geradoEm = geradoEm; }
    public String getExpiraEm() { return expiraEm; }
    public void setExpiraEm(String expiraEm) { this.expiraEm = expiraEm; }
    public long getSegundosRestantes() { return segundosRestantes; }
    public void setSegundosRestantes(long segundosRestantes) { this.segundosRestantes = segundosRestantes; }
    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }
}
