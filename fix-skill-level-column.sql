-- Script para solucionar el problema de nivel de habilidad con decimales
-- Este script cambia el tipo de la columna skill_level de INTEGER a DOUBLE PRECISION
-- para permitir valores decimales como 7.5, 8.2, etc.

-- Cambiar el tipo de columna skill_level a DOUBLE PRECISION (equivalente a double en Java)
ALTER TABLE players 
ALTER COLUMN skill_level TYPE DOUBLE PRECISION;

-- Verificar el cambio
-- SELECT column_name, data_type 
-- FROM information_schema.columns 
-- WHERE table_name = 'players' AND column_name = 'skill_level';
