package com.example.loginapi.dto;

import java.math.BigDecimal;

public class PasseResponse {
    private Long id;
    private Long idCliente;
    private String tipoPasseNome;
    private String coroaNome;
    private String tipoEstatutoAplicado;
    private BigDecimal precoAplicado;
    private String estadoComercial;
    private String estadoOperacional;
    private String codigoQr;
    private String dataInicio;
    private String dataFim;
    private String criadoEm;
    private String faturaNumero;
    private boolean temFotoPasse;
    private String fotoPasseUrl;
    private String nomeTitular;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdCliente() { return idCliente; }
    public void setIdCliente(Long idCliente) { this.idCliente = idCliente; }
    public String getTipoPasseNome() { return tipoPasseNome; }
    public void setTipoPasseNome(String tipoPasseNome) { this.tipoPasseNome = tipoPasseNome; }
    public String getCoroaNome() { return coroaNome; }
    public void setCoroaNome(String coroaNome) { this.coroaNome = coroaNome; }
    public String getTipoEstatutoAplicado() { return tipoEstatutoAplicado; }
    public void setTipoEstatutoAplicado(String tipoEstatutoAplicado) { this.tipoEstatutoAplicado = tipoEstatutoAplicado; }
    public BigDecimal getPrecoAplicado() { return precoAplicado; }
    public void setPrecoAplicado(BigDecimal precoAplicado) { this.precoAplicado = precoAplicado; }
    public String getEstadoComercial() { return estadoComercial; }
    public void setEstadoComercial(String estadoComercial) { this.estadoComercial = estadoComercial; }
    public String getEstadoOperacional() { return estadoOperacional; }
    public void setEstadoOperacional(String estadoOperacional) { this.estadoOperacional = estadoOperacional; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }
    public String getCriadoEm() { return criadoEm; }
    public void setCriadoEm(String criadoEm) { this.criadoEm = criadoEm; }
    public String getFaturaNumero() { return faturaNumero; }
    public void setFaturaNumero(String faturaNumero) { this.faturaNumero = faturaNumero; }
    public boolean isTemFotoPasse() { return temFotoPasse; }
    public void setTemFotoPasse(boolean temFotoPasse) { this.temFotoPasse = temFotoPasse; }
    public String getFotoPasseUrl() { return fotoPasseUrl; }
    public void setFotoPasseUrl(String fotoPasseUrl) { this.fotoPasseUrl = fotoPasseUrl; }
    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }
}
