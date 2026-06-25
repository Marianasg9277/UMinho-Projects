-- Explicit DDL for passe_qr_tokens.
-- Runs after Hibernate DDL (spring.jpa.defer-datasource-initialization=true),
-- so passes table is guaranteed to exist when the FK is evaluated.
-- All statements are idempotent: safe on both fresh and existing databases.

CREATE TABLE IF NOT EXISTS passe_qr_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    passe_id    BIGINT       NOT NULL REFERENCES passes (id),
    token       VARCHAR(120) NOT NULL,
    gerado_em   TIMESTAMPTZ  NOT NULL,
    expira_em   TIMESTAMPTZ  NOT NULL,
    revogado_em TIMESTAMPTZ,
    CONSTRAINT uq_passe_qr_tokens_token UNIQUE (token)
    -- uq_passe_qr_tokens_token also serves as the index on token
);

CREATE INDEX IF NOT EXISTS idx_passe_qr_tokens_passe_id
    ON passe_qr_tokens (passe_id);

-- Alinhar a constraint de estado_pagamento com o enum EstadoPagamento do backend.
-- O Hibernate gerou esta constraint antes de USED ser adicionado ao enum;
-- ddl-auto=update não modifica constraints existentes, por isso corrigimos aqui.
-- Idempotente: seguro em DBs novas (a constraint ainda não existe) e existentes.
DO $$
BEGIN
    ALTER TABLE transacoes
        DROP CONSTRAINT IF EXISTS transacoes_estado_pagamento_check;
    ALTER TABLE transacoes
        ADD CONSTRAINT transacoes_estado_pagamento_check
        CHECK (estado_pagamento IN (
            'NOT_STARTED','PENDING','PAID','CANCELLED','FAILED','USED'
        ));
EXCEPTION WHEN undefined_table THEN
    -- tabela ainda não existe (fresh DB antes do Hibernate correr) — ignorar
    NULL;
END;
$$;

-- ─────────────────────────────────────────────────────────────────────────────
-- Role MOTORISTA: alargar coluna e atualizar CHECK constraint em utilizadores.
-- length=10 era insuficiente para 'MOTORISTA' (9 chars, OK) mas não para
-- expansões futuras. Alteramos para VARCHAR(20) para margem segura.
-- Idempotente: ALTER TYPE é seguro em qualquer estado da coluna.
-- ─────────────────────────────────────────────────────────────────────────────
DO $$
BEGIN
    -- 1. Alargar coluna role para VARCHAR(20) (pode já estar alargada — seguro)
    ALTER TABLE utilizadores ALTER COLUMN role TYPE VARCHAR(20);

    -- 2. Atualizar CHECK constraint para incluir as roles operacionais
    ALTER TABLE utilizadores
        DROP CONSTRAINT IF EXISTS utilizadores_role_check;
    ALTER TABLE utilizadores
        ADD CONSTRAINT utilizadores_role_check
        CHECK (role IN ('ADMIN', 'CLIENTE', 'MOTORISTA', 'FISCALIZADOR', 'GESTOR_SERVICOS', 'GESTOR_FROTAS'));
EXCEPTION WHEN undefined_table THEN
    NULL;
END;
$$;

-- ─────────────────────────────────────────────────────────────────────────────
-- Utilizador de teste: MOTORISTA
-- Password: motorista123  (BCrypt com cost=10)
-- Não tem entidade Cliente associada — intencional.
-- ON CONFLICT DO NOTHING: seguro em reruns e DBs existentes.
-- ─────────────────────────────────────────────────────────────────────────────
INSERT INTO utilizadores (email, password, role)
VALUES (
    'motorista@tub.pt',
    '$2a$10$bFbtS/kftjgYy5qRKlnwdukN3melGU2a9HK6WnfIbTcerHAatXLWi',
    'MOTORISTA'
)
ON CONFLICT (email) DO NOTHING;


-- ─────────────────────────────────────────────────────────────────────────────
-- Campos de referência GTFS reais.
-- Permitem separar IDs internos da BD dos identificadores dos ficheiros GTFS.
-- Idempotente para bases existentes.
-- ─────────────────────────────────────────────────────────────────────────────
DO $$
BEGIN
    ALTER TABLE linhas ADD COLUMN IF NOT EXISTS gtfs_route_id VARCHAR(30);
    ALTER TABLE paragens ADD COLUMN IF NOT EXISTS gtfs_stop_id VARCHAR(50);
    ALTER TABLE paragens ADD COLUMN IF NOT EXISTS zone_id VARCHAR(20);
    ALTER TABLE tipos_bilhete ADD COLUMN IF NOT EXISTS gtfs_fare_id VARCHAR(80);
    ALTER TABLE tipos_bilhete ADD COLUMN IF NOT EXISTS transfer_duration INTEGER;
    ALTER TABLE tipos_bilhete ADD COLUMN IF NOT EXISTS transfers INTEGER;
    ALTER TABLE linha_paragens ADD COLUMN IF NOT EXISTS sentido VARCHAR(20);
    ALTER TABLE rota_linha_ponto ADD COLUMN IF NOT EXISTS shape_id VARCHAR(80);
EXCEPTION WHEN undefined_table THEN
    NULL;
END;
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_linhas_gtfs_route_id
    ON linhas (gtfs_route_id)
    WHERE gtfs_route_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_paragens_gtfs_stop_id
    ON paragens (gtfs_stop_id)
    WHERE gtfs_stop_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_tipos_bilhete_gtfs_fare_id
    ON tipos_bilhete (gtfs_fare_id)
    WHERE gtfs_fare_id IS NOT NULL;



-- A constraint antiga unique_ordem_por_linha impedia guardar IDA e VOLTA,
-- porque ambos os sentidos começam na ordem 1. A unicidade correta passa a ser
-- por linha + sentido + ordem.
DO $$
BEGIN
    ALTER TABLE linha_paragens
        DROP CONSTRAINT IF EXISTS unique_ordem_por_linha;
EXCEPTION WHEN undefined_table THEN
    NULL;
END;
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_linha_paragens_linha_sentido_ordem
    ON linha_paragens (linha_id, sentido, ordem)
    WHERE sentido IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_linha_paragens_linha_sentido_ordem
    ON linha_paragens (linha_id, sentido, ordem);

CREATE INDEX IF NOT EXISTS idx_rota_linha_ponto_linha_sentido_ordem
    ON rota_linha_ponto (linha_id, sentido, ordem);

CREATE INDEX IF NOT EXISTS idx_rota_linha_ponto_shape_id
    ON rota_linha_ponto (shape_id);

-- ─────────────────────────────────────────────────────────────────────────────
-- Pagamentos: remover constraint UNIQUE em passe_id.
-- A relação Pagamento->Passe deve ser ManyToOne para suportar renovações.
-- A constraint foi gerada pelo Hibernate quando a anotação era @OneToOne com
-- unique=true (@JoinColumn name="passe_id", unique=true).
-- Idempotente: DROP CONSTRAINT IF EXISTS é seguro em reruns.
-- A FK pagamentos(passe_id)->passes(id) é mantida — só o UNIQUE é removido.
-- ─────────────────────────────────────────────────────────────────────────────
DO $$
BEGIN
    ALTER TABLE pagamentos DROP CONSTRAINT IF EXISTS ukcxn4o5nn31xjkb3hn6nx8q20t;
EXCEPTION WHEN undefined_table THEN
    NULL;
END;
$$;

-- Historico de validacoes de passes e bilhetes.
-- O projeto usa Hibernate ddl-auto=update; este bloco mantem a criacao
-- idempotente quando schema.sql estiver ativo em ambientes locais.
CREATE TABLE IF NOT EXISTS historico_validacoes (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES clientes (id),
    data_validacao TIMESTAMP NOT NULL,
    tipo_titulo VARCHAR(20) NOT NULL,
    transacao_id BIGINT REFERENCES transacoes (id),
    passe_id BIGINT REFERENCES passes (id),
    linha_id BIGINT REFERENCES linhas (id),
    tipo_descricao VARCHAR(100),
    detalhes VARCHAR(200)
);

CREATE INDEX IF NOT EXISTS idx_cliente_data
    ON historico_validacoes (cliente_id, data_validacao DESC);

-- Estado operacional e ocupação do autocarro
-- A capacidade será preenchida pelo script de simulação até o caso de uso
-- "Adicionar Veículo" ser implementado no backoffice.
CREATE TABLE IF NOT EXISTS autocarro_estado (
    autocarro_id  BIGINT PRIMARY KEY REFERENCES autocarros(id),
    estado        VARCHAR(30) NOT NULL DEFAULT 'ARMAZENADO',
    sub_estado    VARCHAR(30),
    ocupacao      INTEGER NOT NULL DEFAULT 0,
    capacidade    INTEGER NOT NULL DEFAULT 60,
    atualizado_em TIMESTAMP NOT NULL DEFAULT NOW()
);
