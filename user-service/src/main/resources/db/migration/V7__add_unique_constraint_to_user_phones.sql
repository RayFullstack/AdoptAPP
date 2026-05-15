-- V7__add_unique_constraint_to_user_phones.sql
-- Agrega unique constraint a user_phones.number para coincidir con JPA

ALTER TABLE user_phones ADD CONSTRAINT uq_user_phones_number UNIQUE (number);
