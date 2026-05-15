-- V9__add_passwords_to_v2_users.sql
-- Asigna password a los usuarios creados en V2 (no tenian columna password en ese momento)

UPDATE users SET password = '$2a$10$LAK58ME84bgotvy2eL.eWeobSCHMDsaD3BajXq/swyevMwfw8PW/m'
WHERE username IN ('LisaS', 'HomoSimp', 'StacyBakr') AND password IS NULL;
