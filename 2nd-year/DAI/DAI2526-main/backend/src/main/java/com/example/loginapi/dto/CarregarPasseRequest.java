package com.example.loginapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.loginapi.model.infraestrutura.Coroa;


/**
 * Request DTO for loading/recharging an existing pass.
 * Allows selecting a new pass type, crown (coroa) and payment method.
 */
public class CarregarPasseRequest {

    @NotNull(message = "tipoPasseId é obrigatório")
    private Long tipoPasseId;

    @NotNull(message = "coroaId é obrigatório")
    private Long coroaId;

    /**
     * Payment method: CARTAO, MBWAY or SALDO_CONTA.
     * If omitted, defaults to CARTAO.
     */
    @Pattern(regexp = "CARTAO|MBWAY|SALDO_CONTA", message = "metodoPagamento deve ser CARTAO, MBWAY ou SALDO_CONTA")
    private String metodoPagamento = "CARTAO";

    public Long getTipoPasseId() { return tipoPasseId; }
    public void setTipoPasseId(Long tipoPasseId) { this.tipoPasseId = tipoPasseId; }

    public Long getCoroaId() { return coroaId; }
    public void setCoroaId(Long coroaId) { this.coroaId = coroaId; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    private Long cartaoId;

    public Long getCartaoId() { return cartaoId; }
    public void setCartaoId(Long cartaoId) { this.cartaoId = cartaoId; }
}
