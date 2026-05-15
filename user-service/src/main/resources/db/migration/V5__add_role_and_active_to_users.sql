-- V5__add_role_and_active_to_users.sql
-- Agrega columnas role y active que faltaban en la tabla users

ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ADOPTER';
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
