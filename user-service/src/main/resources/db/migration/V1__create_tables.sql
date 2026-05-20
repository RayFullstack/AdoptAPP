CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'ADOPTER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_phones (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_user_phone_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
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
    CONSTRAINT fk_user_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
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
    comment VARCHAR(255),
    CONSTRAINT fk_user_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_user_addresses_city ON user_addresses(city);
CREATE INDEX idx_user_addresses_country ON user_addresses(country);
CREATE INDEX idx_user_history_user_id ON user_history(user_id);
CREATE INDEX idx_user_history_changed_at ON user_history(changed_at);

ALTER TABLE user_phones ADD CONSTRAINT uq_user_phones_number UNIQUE (number);
