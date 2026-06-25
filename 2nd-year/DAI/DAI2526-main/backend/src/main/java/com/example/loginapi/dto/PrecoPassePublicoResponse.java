package com.example.loginapi.dto;

import java.math.BigDecimal;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;


public class PrecoPassePublicoResponse {

    private String tipoEstatuto;
    private String estatutoLabel;
    private String tipoPasseNome;
    private String coroaNome;
    private BigDecimal preco;
    private int duracaoDias;

    public PrecoPassePublicoResponse() {}

    public PrecoPassePublicoResponse(String tipoEstatuto, String estatutoLabel,
                                      String tipoPasseNome, String coroaNome,
                                      BigDecimal preco, int duracaoDias) {
        this.tipoEstatuto  = tipoEstatuto;
        this.estatutoLabel = estatutoLabel;
        this.tipoPasseNome = tipoPasseNome;
        this.coroaNome     = coroaNome;
        this.preco         = preco;
        this.duracaoDias   = duracaoDias;
    }

    public String getTipoEstatuto()  { return tipoEstatuto; }
    public String getEstatutoLabel() { return estatutoLabel; }
    public String getTipoPasseNome() { return tipoPasseNome; }
    public String getCoroaNome()     { return coroaNome; }
    public BigDecimal getPreco()     { return preco; }
    public int getDuracaoDias()      { return duracaoDias; }
}
