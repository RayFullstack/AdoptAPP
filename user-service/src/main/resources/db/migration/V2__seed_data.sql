-- Users from V2 (original) + DataInitializer
INSERT INTO users (username, name, surname, email, password, role, status, active, created_at) VALUES
    ('LisaS', 'Lisa', 'Simpson', 'lsimpson@mail.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'ADOPTER', 'ACTIVE', TRUE, NOW()),
    ('HomoSimp', 'Homero', 'Simpson', 'homerosimp@mail.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'ADOPTER', 'ACTIVE', TRUE, NOW()),
    ('StacyBakr', 'Stacy', 'Baker', 'fakemail123@mail.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'SHELTER_ADMIN', 'ACTIVE', TRUE, NOW());

-- Users from V6 (original)
INSERT INTO users (username, name, surname, email, password, role, status, active, created_at) VALUES
    ('admin', 'Administrador', 'Admin', 'admin@empresa.com', '$2b$10$muCDGFIIRNG29eEzZF6Dz.RtgnTsUObmxOTff2DMfWWseVcZ.2u/6', 'ADMIN', 'ACTIVE', TRUE, NOW()),
    ('ana.garcia', 'Ana', 'Garcia', 'ana.garcia@empresa.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'ADOPTER', 'ACTIVE', TRUE, NOW()),
    ('carlos.lopez', 'Carlos', 'Lopez', 'carlos.lopez@empresa.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'SHELTER_ADMIN', 'ACTIVE', TRUE, NOW());

-- Users from DataInitializer (H2-only, now also in Postgres)
INSERT INTO users (username, name, surname, email, password, role, status, active, created_at) VALUES
    ('volunteer1', 'Ned', 'Flanders', 'ned.flanders@mail.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'VOLUNTEER', 'ACTIVE', TRUE, NOW()),
    ('dr.hibbert', 'Julius', 'Hibbert', 'dr.hibbert@mail.com', '$2b$10$/fuVEZ3IyHV6HVi09umZJ.MzH3TupZytM29cWzuw3IrIFg2RAUk1.', 'VET', 'ACTIVE', TRUE, NOW()),
    ('admin2', 'Admin', 'AdoptApp', 'admin@adoptapp.com', '$2b$10$muCDGFIIRNG29eEzZF6Dz.RtgnTsUObmxOTff2DMfWWseVcZ.2u/6', 'ADMIN', 'ACTIVE', TRUE, NOW());

-- Phones
INSERT INTO user_phones (number, user_id) VALUES
    ('123456789', 1),
    ('954456881', 2),
    ('9564422331', 3),
    ('111222333', 7),
    ('444555666', 8);

-- Addresses
INSERT INTO user_addresses (country, city, street, home_number, postal_code, type, primary_address, user_id) VALUES
    ('Chile', 'Santiago', 'Av. Siempreviva', '123', '123456', 'HOME', TRUE, 1),
    ('Chile', 'Santiago', 'Av. Siempreviva', '456', '345678', 'HOME', TRUE, 2),
    ('Chile', 'Santiago', 'Av. Real', '7865', '22222455', 'WORK', TRUE, 3),
    ('Chile', 'Santiago', 'Av. Evergreen', '742', '123456', 'HOME', TRUE, 7),
    ('Chile', 'Santiago', 'Av. Medical', '100', '654321', 'WORK', TRUE, 8);
