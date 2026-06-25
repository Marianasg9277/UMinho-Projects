package com.example.loginapi.dto;

import java.time.LocalDateTime;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoBilhete;


public class ValidacaoRegistadaDTO {

    private Long id;
    private LocalDateTime dataValidacao;
    private String tipoTitulo;
    private String nomeTitular;
    private String linha;
    private String tipoDescricao;
    private boolean resultado;
    private String motivoRejeicao;

    // Campos de passe
    private String tipoPasse;
    private String coroa;
    private String estatuto;
    private String validadeInicio;
    private String validadeFim;
    private String estadoPasse;

    // Campos de bilhete
    private String tipoBilhete;
    private String dataCompra;
    private String estadoBilhete;

    public ValidacaoRegistadaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getDataValidacao() { return dataValidacao; }
    public void setDataValidacao(LocalDateTime dataValidacao) { this.dataValidacao = dataValidacao; }

    public String getTipoTitulo() { return tipoTitulo; }
    public void setTipoTitulo(String tipoTitulo) { this.tipoTitulo = tipoTitulo; }

    public String getNomeTitular() { return nomeTitular; }
    public void setNomeTitular(String nomeTitular) { this.nomeTitular = nomeTitular; }

    public String getLinha() { return linha; }
    public void setLinha(String linha) { this.linha = linha; }

    public String getTipoDescricao() { return tipoDescricao; }
    public void setTipoDescricao(String tipoDescricao) { this.tipoDescricao = tipoDescricao; }

    public boolean isResultado() { return resultado; }
    public void setResultado(boolean resultado) { this.resultado = resultado; }

    public String getMotivoRejeicao() { return motivoRejeicao; }
    public void setMotivoRejeicao(String motivoRejeicao) { this.motivoRejeicao = motivoRejeicao; }

    public String getTipoPasse() { return tipoPasse; }
    public void setTipoPasse(String tipoPasse) { this.tipoPasse = tipoPasse; }

    public String getCoroa() { return coroa; }
    public void setCoroa(String coroa) { this.coroa = coroa; }

    public String getEstatuto() { return estatuto; }
    public void setEstatuto(String estatuto) { this.estatuto = estatuto; }

    public String getValidadeInicio() { return validadeInicio; }
    public void setValidadeInicio(String validadeInicio) { this.validadeInicio = validadeInicio; }

    public String getValidadeFim() { return validadeFim; }
    public void setValidadeFim(String validadeFim) { this.validadeFim = validadeFim; }

    public String getEstadoPasse() { return estadoPasse; }
    public void setEstadoPasse(String estadoPasse) { this.estadoPasse = estadoPasse; }

    public String getTipoBilhete() { return tipoBilhete; }
    public void setTipoBilhete(String tipoBilhete) { this.tipoBilhete = tipoBilhete; }

    public String getDataCompra() { return dataCompra; }
    public void setDataCompra(String dataCompra) { this.dataCompra = dataCompra; }

    public String getEstadoBilhete() { return estadoBilhete; }
    public void setEstadoBilhete(String estadoBilhete) { this.estadoBilhete = estadoBilhete; }
}
