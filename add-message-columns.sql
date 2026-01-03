-- Script para agregar las nuevas columnas a la tabla group_messages
-- Ejecutar este script en la base de datos antes de usar las nuevas funcionalidades

ALTER TABLE group_messages 
ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS is_highlighted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS edited_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;

-- Crear tabla para mensajes eliminados por usuario
CREATE TABLE IF NOT EXISTS deleted_messages (
    id VARCHAR(255) PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL,
    user_id BIGINT NOT NULL,
    deleted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (message_id) REFERENCES group_messages(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_deleted_message (message_id, user_id)
);


