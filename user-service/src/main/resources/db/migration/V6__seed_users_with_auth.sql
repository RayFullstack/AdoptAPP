-- V6__seed_users_with_auth.sql
-- Inserta usuarios con roles y autenticación

INSERT INTO users (name, surname, username, email, role, status, active, password, created_at) VALUES
    ('Administrador', 'Admin', 'admin', 'admin@empresa.com', 'ADMIN', 'ACTIVE', TRUE, '$2a$10$gT.PsFi3xTq9xc3virQAfesYBesY5g53tQ5R7lgJGqgVdVMH0I8qa', NOW()),
    ('Ana Garcia', 'Garcia', 'ana.garcia', 'ana.garcia@empresa.com', 'ADOPTER', 'ACTIVE', TRUE, '$2a$10$LAK58ME84bgotvy2eL.eWeobSCHMDsaD3BajXq/swyevMwfw8PW/m', NOW()),
    ('Carlos Lopez', 'Lopez', 'carlos.lopez', 'carlos.lopez@empresa.com', 'SHELTER', 'ACTIVE', TRUE, '$2a$10$LAK58ME84bgotvy2eL.eWeobSCHMDsaD3BajXq/swyevMwfw8PW/m', NOW());