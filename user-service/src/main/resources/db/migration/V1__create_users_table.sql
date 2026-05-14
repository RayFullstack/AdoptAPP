-- V1__create_users_tables.sql
-- Crea las tablas iniciales del microservicio user-service

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       name VARCHAR(50) NOT NULL,
                       surname VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_phones (
                             id BIGSERIAL PRIMARY KEY,
                             number VARCHAR(20) NOT NULL,
                             user_id BIGINT NOT NULL UNIQUE,

                             CONSTRAINT fk_user_phone_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE
);

CREATE TABLE user_addresses (
                                id BIGSERIAL PRIMARY KEY,
                                country VARCHAR(100) NOT NULL,
                                city VARCHAR(100) NOT NULL,
                                street VARCHAR(150) NOT NULL,
                                home_number VARCHAR(20) NOT NULL,
                                postal_code VARCHAR(20) NOT NULL,
                                type VARCHAR(50) NOT NULL,
                                primary_address BOOLEAN NOT NULL DEFAULT TRUE,
                                user_id BIGINT NOT NULL,

                                CONSTRAINT fk_user_address_user
                                    FOREIGN KEY (user_id)
                                        REFERENCES users(id)
                                        ON DELETE CASCADE
);

CREATE INDEX idx_users_username
    ON users(username);

CREATE INDEX idx_users_email
    ON users(email);

CREATE INDEX idx_users_status
    ON users(status);

CREATE INDEX idx_user_addresses_city
    ON user_addresses(city);

CREATE INDEX idx_user_addresses_country
    ON user_addresses(country);