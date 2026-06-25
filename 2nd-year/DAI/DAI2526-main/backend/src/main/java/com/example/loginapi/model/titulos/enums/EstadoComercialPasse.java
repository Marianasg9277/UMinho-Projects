package com.example.loginapi.model.titulos.enums;

import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;


/**
 * Estado comercial do passe (relativo ao pagamento).
 */
public enum EstadoComercialPasse {
    PENDING_PAYMENT,  // Aguarda pagamento
    PAID,             // Pago
    CANCELLED         // Cancelado
}
