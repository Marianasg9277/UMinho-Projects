package com.example.loginapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;


/**
 * Pedido de renovação de passe.
 * O campo metodo indica a forma de pagamento da renovação.
 */
public class RenovarPasseRequest {

    @NotNull(message = "passeId é obrigatório")
    private Long passeId;

    @Pattern(regexp = "CARTAO|MBWAY|SALDO_CONTA", message = "metodo deve ser CARTAO, MBWAY ou SALDO_CONTA")
    private String metodo = "CARTAO";

    private Long cartaoId;

    public Long getPasseId() { return passeId; }
    public void setPasseId(Long passeId) { this.passeId = passeId; }

    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }

    public Long getCartaoId() { return cartaoId; }
    public void setCartaoId(Long cartaoId) { this.cartaoId = cartaoId; }
}
