CREATE TABLE pet_health (
    id BIGSERIAL PRIMARY KEY,
    vaccinated BOOLEAN NOT NULL DEFAULT FALSE,
    sterilized BOOLEAN NOT NULL DEFAULT FALSE,
    diseases VARCHAR(255) NOT NULL DEFAULT ''
);

CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    species VARCHAR(50) NOT NULL,
    race VARCHAR(50) NOT NULL,
    color VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    size VARCHAR(50) NOT NULL,
    personality VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    foster_id BIGINT NOT NULL,
    health_id BIGINT UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pets_health FOREIGN KEY (health_id) REFERENCES pet_health(id) ON DELETE SET NULL
);

CREATE INDEX idx_pets_status ON pets(status);
CREATE INDEX idx_pets_created_at ON pets(created_at);
CREATE INDEX idx_pets_name ON pets(name);
CREATE INDEX idx_pets_species ON pets(species);
CREATE INDEX idx_pets_foster_id ON pets(foster_id);
