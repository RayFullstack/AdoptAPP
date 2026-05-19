CREATE TABLE shelters (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE shelter_history (
    id BIGSERIAL PRIMARY KEY,
    shelter_id BIGINT NOT NULL,
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
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shelter_history_shelter FOREIGN KEY (shelter_id) REFERENCES shelters(id) ON DELETE CASCADE
);

CREATE INDEX idx_shelters_status ON shelters(status);
CREATE INDEX idx_shelter_history_shelter_id ON shelter_history(shelter_id);
