-- ============================================
-- SCRIPT DE OPTIMIZACIÓN: ÍNDICES PARA BASE DE DATOS
-- ============================================
-- Este script crea índices para optimizar las consultas más frecuentes
-- Ejecutar en Supabase SQL Editor después de crear las tablas

-- Índices para la tabla PLAYERS
-- Optimiza búsquedas por usuario (muy frecuente)
CREATE INDEX IF NOT EXISTS idx_players_user_id ON players(user_id);

-- Optimiza búsquedas por nombre (case insensitive) y usuario
CREATE INDEX IF NOT EXISTS idx_players_name_user ON players(LOWER(name), user_id);

-- Optimiza búsquedas de jugadores con asistencia marcada
CREATE INDEX IF NOT EXISTS idx_players_attended_user ON players(attended, user_id) WHERE attended = true;

-- Optimiza búsquedas de jugadores activos por usuario
CREATE INDEX IF NOT EXISTS idx_players_activo_user ON players(activo, user_id) WHERE activo = true;

-- Optimiza ordenamiento por goles (top scorers)
CREATE INDEX IF NOT EXISTS idx_players_goals_user ON players(user_id, goals_scored DESC);

-- Optimiza búsquedas por deuda
CREATE INDEX IF NOT EXISTS idx_players_debt_user ON players(user_id, total_debt, total_paid);

-- Índices para la tabla GOALS
-- Optimiza búsquedas por usuario
CREATE INDEX IF NOT EXISTS idx_goals_user_id ON goals(user_id);

-- Optimiza búsquedas por partido
CREATE INDEX IF NOT EXISTS idx_goals_match_id ON goals(match_id);

-- Optimiza búsquedas por jugador
CREATE INDEX IF NOT EXISTS idx_goals_player_id ON goals(player_id);

-- Optimiza búsquedas por timestamp (para ordenamiento)
CREATE INDEX IF NOT EXISTS idx_goals_timestamp ON goals(timestamp DESC);

-- Índices para la tabla PAYMENTS
-- Optimiza búsquedas por usuario
CREATE INDEX IF NOT EXISTS idx_payments_user_id ON payments(user_id);

-- Optimiza búsquedas por jugador
CREATE INDEX IF NOT EXISTS idx_payments_player_id ON payments(player_id);

-- Optimiza búsquedas por fecha (para ordenamiento)
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(date DESC);

-- Índices para la tabla MATCHES
-- Optimiza búsquedas por usuario
CREATE INDEX IF NOT EXISTS idx_matches_user_id ON matches(user_id);

-- Optimiza búsquedas de partidos activos (muy frecuente)
CREATE INDEX IF NOT EXISTS idx_matches_active_user ON matches(active, user_id) WHERE active = true;

-- Optimiza búsquedas por fecha
CREATE INDEX IF NOT EXISTS idx_matches_start_date ON matches(start_date DESC);

-- Índices para la tabla USERS
-- Optimiza búsquedas por email (login)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ============================================
-- VERIFICACIÓN DE ÍNDICES CREADOS
-- ============================================
-- Ejecutar para verificar que los índices se crearon correctamente:
-- SELECT schemaname, tablename, indexname 
-- FROM pg_indexes 
-- WHERE tablename IN ('players', 'goals', 'payments', 'matches', 'users')
-- ORDER BY tablename, indexname;

