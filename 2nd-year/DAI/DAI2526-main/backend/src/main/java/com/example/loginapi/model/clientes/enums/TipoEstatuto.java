package com.example.loginapi.model.clientes.enums;

/**
 * Tipos de estatuto disponíveis no sistema.
 * SEM_ESTATUTO é o valor por defeito — nunca usar null.
 */
public enum TipoEstatuto {
    SEM_ESTATUTO,    // Sem estatuto aprovado (valor por defeito)
    ESTUDANTE,       // Exige comprovativo de inscrição
    RESIDENTE,       // Exige morada fiscal / comprovativo
    SENIOR,          // Automático: idade >= limiar configurável (default 65)
    MILITAR,         // Exige cédula militar
    CRIANCA,         // Automático: idade <= limiar configurável (default 12)
    INCAPACITADO;    // Exige documentação — válido até revogação

    /**
     * Indica se este estatuto deve ser validado automaticamente (por idade).
     */
    public boolean isAutomatico() {
        return this == SENIOR || this == CRIANCA;
    }

    /**
     * Indica se este estatuto exige submissão de documentos e revisão manual.
     */
    public boolean exigeDocumentos() {
        return this == ESTUDANTE || this == RESIDENTE || this == MILITAR || this == INCAPACITADO;
    }
}
