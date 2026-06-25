package com.example.loginapi.dto;

import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoBilhete;


public class FiscalizacaoQrResponse {

    private boolean valido;
    private String mensagem;
    private String tipoTitulo;      // "PASSE" | "BILHETE"
    private String estado;          // estadoOperacional do passe ou estadoPagamento do bilhete
    private String motivoInvalidade;

    // Campos comuns
    private String nomeTitular;
    private String coroa;
    private String validadeFim;

    // Campos de passe
    private String tipoPasse;
    private String estatuto;
    private String validadeInicio;

    // Campos de bilhete
    private String tipoBilhete;
    private String linha;
    private String dataCompra;

    public FiscalizacaoQrResponse() {}

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(String tipoTitulo) { this.tipoTitulo = tipoTitulo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getMotivoInvalidade() { return motivoInvalidade; }
    public void setMotivoInvalidade(String motivoInvalidade) { this.motivoInvalidade = motivoInvalidade; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getCoroa() { return coroa; }
    public void setCoroa(String coroa) { this.coroa = coroa; }

    public String getValidadeFim() { return validadeFim; }
    public void setValidadeFim(String validadeFim) { this.validadeFim = validadeFim; }

    public String getTipoPasse() { return tipoPasse; }
    public void setTipoPasse(String tipoPasse) { this.tipoPasse = tipoPasse; }

    public String getEstatuto() { return estatuto; }
    public void setEstatuto(String estatuto) { this.estatuto = estatuto; }

    public String getValidadeInicio() { return validadeInicio; }
    public void setValidadeInicio(String validadeInicio) { this.validadeInicio = validadeInicio; }

    public String getTipoBilhete() { return tipoBilhete; }
    public void setTipoBilhete(String tipoBilhete) { this.tipoBilhete = tipoBilhete; }

    public String getLinha() { return linha; }
    public void setLinha(String linha) { this.linha = linha; }

    public String getDataCompra() { return dataCompra; }
    public void setDataCompra(String dataCompra) { this.dataCompra = dataCompra; }
}
