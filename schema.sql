-- =============================================
-- AdoptApp - Complete Database Schema
-- PostgreSQL
-- =============================================

-- =============================================
-- 1. USER-SERVICE DATABASE (users_db)
-- =============================================

DROP DATABASE IF EXISTS users_db WITH (FORCE);
CREATE DATABASE users_db;

\c users_db

DROP TABLE IF EXISTS user_history CASCADE;
DROP TABLE IF EXISTS user_phones CASCADE;
DROP TABLE IF EXISTS user_addresses CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(20) NOT NULL DEFAULT 'ADOPTER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE user_phones (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(20) NOT NULL UNIQUE,
    user_id BIGINT UNIQUE REFERENCES users(id)
);

CREATE TABLE user_addresses (
    id BIGSERIAL PRIMARY KEY,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    street VARCHAR(100) NOT NULL,
    home_number VARCHAR(20) NOT NULL,
    postal_code VARCHAR(100) NOT NULL,
    type VARCHAR(100) NOT NULL,
    primary_address BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id)
);

CREATE TABLE user_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_name VARCHAR(50),
    new_name VARCHAR(50),
    previous_surname VARCHAR(50),
    new_surname VARCHAR(50),
    previous_username VARCHAR(50),
    new_username VARCHAR(50),
    previous_email VARCHAR(150),
    new_email VARCHAR(150),
    previous_phone VARCHAR(100),
    new_phone VARCHAR(100),
    previous_role VARCHAR(20),
    new_role VARCHAR(20),
    previous_active BOOLEAN,
    new_active BOOLEAN,
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(255)
);


-- =============================================
-- 2. PET-SERVICE DATABASE (pet_db)
-- =============================================

DROP DATABASE IF EXISTS pet_db WITH (FORCE);
CREATE DATABASE pet_db;

\c pet_db

DROP TABLE IF EXISTS pet_history CASCADE;
DROP TABLE IF EXISTS pets CASCADE;

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    species VARCHAR(50) NOT NULL,
    race VARCHAR(50) NOT NULL,
    color VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 0),
    size VARCHAR(50) NOT NULL,
    personality VARCHAR(50) NOT NULL,
    foster_id BIGINT NOT NULL,
    shelter_id BIGINT,
    status VARCHAR(50) NOT NULL,
    health_service_id BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE pet_history (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets(id),
    previous_name VARCHAR(20),
    new_name VARCHAR(20),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_foster_id BIGINT,
    new_foster_id BIGINT,
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(255)
);


-- =============================================
-- 3. ADOPTION-SERVICE DATABASE (adoption_db)
-- =============================================

DROP DATABASE IF EXISTS adoption_db WITH (FORCE);
CREATE DATABASE adoption_db;

\c adoption_db

DROP TABLE IF EXISTS adoption_history CASCADE;
DROP TABLE IF EXISTS adoptions CASCADE;

CREATE TABLE adoptions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    pet_id BIGINT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE adoption_history (
    id BIGSERIAL PRIMARY KEY,
    adoption_id BIGINT NOT NULL REFERENCES adoptions(id),
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP
);


-- =============================================
-- 4. NOTIFICATION-SERVICE DATABASE (notif_db)
-- =============================================

DROP DATABASE IF EXISTS notif_db;
CREATE DATABASE notif_db;

\c notif_db

DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS notification_types CASCADE;

CREATE TABLE notification_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    template TEXT NOT NULL,
    channel VARCHAR(20) NOT NULL
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    recipient VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type_id BIGINT NOT NULL REFERENCES notification_types(id),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP
);


-- =============================================
-- 5. HEALTH-SERVICE DATABASE (health_db)
-- =============================================

DROP DATABASE IF EXISTS health_db;
CREATE DATABASE health_db;

\c health_db

DROP TABLE IF EXISTS health_history CASCADE;
DROP TABLE IF EXISTS health CASCADE;

CREATE TABLE health (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    sterilization_status VARCHAR(20) NOT NULL,
    vaccination_status VARCHAR(20) NOT NULL,
    diseases VARCHAR(500) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE health_history (
    id BIGSERIAL PRIMARY KEY,
    health_id BIGINT NOT NULL REFERENCES health(id),
    previous_sterilization_status VARCHAR(150),
    new_sterilization_status VARCHAR(150),
    previous_vaccination_status VARCHAR(150),
    new_vaccination_status VARCHAR(150),
    previous_disease VARCHAR(150),
    new_disease VARCHAR(150),
    action VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(255),
    changed_by_user_id BIGINT
);


-- =============================================
-- 6. FOLLOWUP-SERVICE DATABASE (followup_db)
-- =============================================

DROP DATABASE IF EXISTS followup_db;
CREATE DATABASE followup_db;

\c followup_db

DROP TABLE IF EXISTS followup_history CASCADE;
DROP TABLE IF EXISTS followups CASCADE;

CREATE TABLE followups (
    id BIGSERIAL PRIMARY KEY,
    adopter_name VARCHAR(100) NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    user_id BIGINT,
    pet_id BIGINT,
    adoption_id BIGINT,
    visit_date TIMESTAMP NOT NULL,
    comments TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE followup_history (
    id BIGSERIAL PRIMARY KEY,
    followup_id BIGINT NOT NULL REFERENCES followups(id),
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL
);


-- =============================================
-- 7. DONATION-SERVICE DATABASE (donation_db)
-- =============================================

DROP DATABASE IF EXISTS donation_db;
CREATE DATABASE donation_db;

\c donation_db

DROP TABLE IF EXISTS donation_history CASCADE;
DROP TABLE IF EXISTS donations CASCADE;

CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    donor_name VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    shelter_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE donation_history (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL REFERENCES donations(id),
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_amount DECIMAL(12,2),
    new_amount DECIMAL(12,2),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL
);


-- =============================================
-- 8. STAFF-SERVICE DATABASE (staff_db)
-- =============================================

DROP DATABASE IF EXISTS staff_db;
CREATE DATABASE staff_db;

\c staff_db

DROP TABLE IF EXISTS staff_history CASCADE;
DROP TABLE IF EXISTS staff CASCADE;

CREATE TABLE staff (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shelter_id BIGINT NOT NULL,
    position VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    hire_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE staff_history (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL REFERENCES staff(id),
    action VARCHAR(50) NOT NULL,
    previous_position VARCHAR(20),
    new_position VARCHAR(20),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_phone VARCHAR(20),
    new_phone VARCHAR(20),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL
);


-- =============================================
-- 9. SUPPLY-SERVICE DATABASE (supply_db)
-- =============================================

DROP DATABASE IF EXISTS supply_db;
CREATE DATABASE supply_db;

\c supply_db

DROP TABLE IF EXISTS supplies_history CASCADE;
DROP TABLE IF EXISTS supplies CASCADE;

CREATE TABLE supplies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL,
    unit VARCHAR(50) NOT NULL,
    category VARCHAR(20) NOT NULL,
    shelter_id BIGINT NOT NULL,
    supplier_name VARCHAR(200),
    minimum_stock INTEGER NOT NULL DEFAULT 5,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE supplies_history (
    id BIGSERIAL PRIMARY KEY,
    supply_id BIGINT NOT NULL REFERENCES supplies(id),
    action VARCHAR(50) NOT NULL,
    comment TEXT,
    prev_status VARCHAR(20),
    new_status VARCHAR(20),
    prev_quantity INTEGER,
    new_quantity INTEGER,
    prev_category VARCHAR(20),
    new_category VARCHAR(20),
    changed_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL
);


-- =============================================
-- 10. SHELTER-SERVICE DATABASE (shelter_db)
-- =============================================

DROP DATABASE IF EXISTS shelter_db;
CREATE DATABASE shelter_db;

\c shelter_db

DROP TABLE IF EXISTS shelter_history CASCADE;
DROP TABLE IF EXISTS shelters CASCADE;

CREATE TABLE shelters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE shelter_history (
    id BIGSERIAL PRIMARY KEY,
    shelter_id BIGINT NOT NULL REFERENCES shelters(id),
    action VARCHAR(50) NOT NULL,
    previous_name VARCHAR(100),
    new_name VARCHAR(100),
    previous_email VARCHAR(100),
    new_email VARCHAR(100),
    previous_phone VARCHAR(20),
    new_phone VARCHAR(20),
    previous_description TEXT,
    new_description TEXT,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_active BOOLEAN,
    new_active BOOLEAN,
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL
);
