-- Script para solucionar el error de "value too long for type character varying(500)"
-- Este script cambia el tipo de la columna photo_url de VARCHAR(500) a TEXT
-- para permitir guardar imágenes en base64 u otras URLs largas

-- Cambiar el tipo de columna photo_url a TEXT
ALTER TABLE groups 
ALTER COLUMN photo_url TYPE TEXT;

-- Verificar el cambio
-- SELECT column_name, data_type, character_maximum_length 
-- FROM information_schema.columns 
-- WHERE table_name = 'groups' AND column_name = 'photo_url';

