package com.example.loginapi.dto;

import java.math.BigDecimal;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;


public class OpcaoPasseResponse {
    private Long tipoPasseId;
    private String tipoPasseNome;
    private int duracaoDias;
    private Long coroaId;
    private String coroaNome;
    private String tipoEstatuto;
    private BigDecimal preco;
    private Long regraPrecoId;

    public OpcaoPasseResponse() {}

    public Long getTipoPasseId() { return tipoPasseId; }
    public void setTipoPasseId(Long tipoPasseId) { this.tipoPasseId = tipoPasseId; }
    public String getTipoPasseNome() { return tipoPasseNome; }
    public void setTipoPasseNome(String tipoPasseNome) { this.tipoPasseNome = tipoPasseNome; }
    public int getDuracaoDias() { return duracaoDias; }
    public void setDuracaoDias(int duracaoDias) { this.duracaoDias = duracaoDias; }
    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }
    public String getCoroaNome() { return coroaNome; }
    public void setCoroaNome(String coroaNome) { this.coroaNome = coroaNome; }
    public String getTipoEstatuto() { return tipoEstatuto; }
    public void setTipoEstatuto(String tipoEstatuto) { this.tipoEstatuto = tipoEstatuto; }
    public BigDecimal getPreco() { return preco; }
    public void setPreco(BigDecimal preco) { this.preco = preco; }
    public Long getRegraPrecoId() { return regraPrecoId; }
    public void setRegraPrecoId(Long regraPrecoId) { this.regraPrecoId = regraPrecoId; }
}
