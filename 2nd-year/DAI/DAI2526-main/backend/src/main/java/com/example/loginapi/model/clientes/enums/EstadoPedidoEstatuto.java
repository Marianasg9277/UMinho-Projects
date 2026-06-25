package com.example.loginapi.model.clientes.enums;

import com.example.loginapi.model.clientes.Utilizador;


/**
 * Estado do pedido de estatuto — workflow completo.
 */
public enum EstadoPedidoEstatuto {
    DRAFT,                  // Rascunho — ainda não submetido
    PENDING_APPROVAL,       // Submetido — aguarda revisão de administrador
    UNDER_REVIEW,           // Em análise por admin
    APPROVED,               // Aprovado manualmente
    REJECTED,               // Rejeitado manualmente
    CORRECTION_REQUESTED,   // Admin pediu correções ao utilizador
    CANCELLED               // Cancelado pelo utilizador
}
