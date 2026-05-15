-- V8__add_role_and_active_to_user_history.sql
-- Agrega columnas para trackear cambios de role y active en el historial

ALTER TABLE user_history ADD COLUMN previous_role VARCHAR(20);
ALTER TABLE user_history ADD COLUMN new_role VARCHAR(20);
ALTER TABLE user_history ADD COLUMN previous_active BOOLEAN;
ALTER TABLE user_history ADD COLUMN new_active BOOLEAN;
