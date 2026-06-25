-- Permite que autocarro.linha_id seja NULL (suporte a desalocação de autocarro).
-- Guard: só aplica o ALTER se a coluna ainda for NOT NULL (safe em fresh DB e existente).
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name  = 'autocarros'
          AND column_name = 'linha_id'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE autocarros ALTER COLUMN linha_id DROP NOT NULL;
    END IF;
END $$;
