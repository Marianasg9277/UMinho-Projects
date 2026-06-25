CREATE TABLE IF NOT EXISTS historico_validacoes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    data_validacao TIMESTAMP NOT NULL,
    tipo_titulo VARCHAR(20) NOT NULL,
    transacao_id BIGINT,
    passe_id BIGINT,
    linha_id BIGINT,
    tipo_descricao VARCHAR(100),
    detalhes VARCHAR(200),

    CONSTRAINT fk_historico_validacoes_cliente
        FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_historico_validacoes_transacao
        FOREIGN KEY (transacao_id) REFERENCES transacoes(id),
    CONSTRAINT fk_historico_validacoes_passe
        FOREIGN KEY (passe_id) REFERENCES passes(id),
    CONSTRAINT fk_historico_validacoes_linha
        FOREIGN KEY (linha_id) REFERENCES linhas(id)
);

CREATE INDEX IF NOT EXISTS idx_cliente_data
    ON historico_validacoes (cliente_id, data_validacao DESC);
