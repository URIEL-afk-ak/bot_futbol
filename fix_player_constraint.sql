-- ============================================
-- SCRIPT PARA CORREGIR RESTRICCIÓN ÚNICA DE JUGADORES
-- ============================================
-- Este script elimina constraints antiguas y asegura que la constraint única
-- sea sobre (name, user_id) para permitir que diferentes usuarios tengan
-- jugadores con el mismo nombre

-- 1. Eliminar constraint antigua si existe (solo por name)
DO $$ 
BEGIN
    -- Intentar eliminar constraint antigua que solo valida name
    IF EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_pblmuavgrnr991e41662asko'
    ) THEN
        ALTER TABLE players DROP CONSTRAINT IF EXISTS uk_pblmuavgrnr991e41662asko;
        RAISE NOTICE 'Constraint antigua eliminada';
    END IF;
END $$;

-- 2. Eliminar cualquier otra constraint única antigua que solo valide name
DO $$ 
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN 
        SELECT conname FROM pg_constraint 
        WHERE conrelid = 'players'::regclass
        AND contype = 'u'
        AND array_length(conkey, 1) = 1
        AND conkey[1] = (SELECT attnum FROM pg_attribute WHERE attrelid = 'players'::regclass AND attname = 'name')
    LOOP
        EXECUTE format('ALTER TABLE players DROP CONSTRAINT IF EXISTS %I', constraint_name);
        RAISE NOTICE 'Constraint única antigua eliminada: %', constraint_name;
    END LOOP;
END $$;

-- 3. Asegurar que existe la constraint correcta (name, user_id)
-- Nota: Esta constraint debería crearse automáticamente por JPA, pero la verificamos
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_player_name_user'
        AND conrelid = 'players'::regclass
    ) THEN
        ALTER TABLE players 
        ADD CONSTRAINT uk_player_name_user 
        UNIQUE (name, user_id);
        RAISE NOTICE 'Constraint única correcta creada: uk_player_name_user';
    ELSE
        RAISE NOTICE 'Constraint única correcta ya existe: uk_player_name_user';
    END IF;
END $$;

-- 4. Verificar que la constraint esté correctamente configurada
SELECT 
    conname as constraint_name,
    pg_get_constraintdef(oid) as constraint_definition
FROM pg_constraint
WHERE conrelid = 'players'::regclass
AND contype = 'u'
ORDER BY conname;

