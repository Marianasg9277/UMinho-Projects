package com.example.loginapi.model.clientes.enums;

import com.example.loginapi.model.clientes.Utilizador;


/**
 * Estado do estatuto ativo de um utilizador.
 */
public enum EstadoEstatutoUtilizador {
    ACTIVE,    // Estatuto válido e em uso
    EXPIRED,   // Expirou (data fim ultrapassada)
    REVOKED,   // Revogado manualmente por admin
    INACTIVE   // Inativo (substituído por outro ou desativado)
}
