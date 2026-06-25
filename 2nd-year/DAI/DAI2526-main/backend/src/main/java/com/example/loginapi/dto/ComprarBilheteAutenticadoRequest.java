package com.example.loginapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.frota.Autocarro;


/**
 * Pedido de compra de bilhete por utilizador autenticado.
 */
public class ComprarBilheteAutenticadoRequest {

    @NotNull(message = "tipoBilheteId é obrigatório")
    @Min(value = 1, message = "tipoBilheteId deve ser >= 1")
    private Long tipoBilheteId;

    /** Linha de autocarro (opcional — alternativa a coroaId). */
    @Min(value = 1, message = "linhaId deve ser >= 1")
    private Long linhaId;

    /** Coroa/zona (opcional — alternativa a linhaId). */
    @Min(value = 1, message = "coroaId deve ser >= 1")
    private Long coroaId;

    @Pattern(regexp = "CARTAO|MBWAY|SALDO_CONTA", message = "metodo deve ser CARTAO, MBWAY ou SALDO_CONTA")
    private String metodo = "CARTAO";

    public Long getTipoBilheteId() { return tipoBilheteId; }
    public void setTipoBilheteId(Long tipoBilheteId) { this.tipoBilheteId = tipoBilheteId; }

    public Long getLinhaId() { return linhaId; }
    public void setLinhaId(Long linhaId) { this.linhaId = linhaId; }

    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
}
