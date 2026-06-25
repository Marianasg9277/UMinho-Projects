package com.example.loginapi.dto;

import com.example.loginapi.model.titulos.TipoBilhete;
import java.math.BigDecimal;

public class TipoBilheteResponse {

    private Long id;
    private String nome;
    private String categoria;
    private BigDecimal preco;
    private Integer validadeHoras;
    private String descricao;
    private boolean ativo;

    public static TipoBilheteResponse from(TipoBilhete t) {
        TipoBilheteResponse r = new TipoBilheteResponse();
        r.id = t.getId();
        r.nome = t.getNome();
        r.categoria = t.getCategoria() != null ? t.getCategoria().name() : null;
        r.preco = t.getPreco();
        r.validadeHoras = t.getValidadeHoras();
        r.descricao = t.getDescricao();
        r.ativo = t.isAtivo();
        return r;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPreco() { return preco; }
    public Integer getValidadeHoras() { return validadeHoras; }
    public String getDescricao() { return descricao; }
    public boolean isAtivo() { return ativo; }
}
