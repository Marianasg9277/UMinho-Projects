package com.example.loginapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.loginapi.model.pagamentos.Pagamento;


public class PagamentoRequest {

    @NotBlank(message = "tipoObjeto é obrigatório")
    private String tipoObjeto;

    @NotNull(message = "objetoId é obrigatório")
    private Long objetoId;

    /**
     * Método de pagamento: CARTAO ou MBWAY.
     * Obrigatório para novos pagamentos; ignorado em consultas.
     */
    @Pattern(regexp = "CARTAO|MBWAY", message = "metodo deve ser CARTAO ou MBWAY")
    private String metodo;

    public String getTipoObjeto() { return tipoObjeto; }
    public void setTipoObjeto(String tipoObjeto) { this.tipoObjeto = tipoObjeto; }

    public Long getObjetoId() { return objetoId; }
    public void setObjetoId(Long objetoId) { this.objetoId = objetoId; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
