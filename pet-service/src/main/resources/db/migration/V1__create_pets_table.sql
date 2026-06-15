CREATE TABLE pets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL,
    species VARCHAR(50) NOT NULL,
    race VARCHAR(50) NOT NULL,
    color VARCHAR(50) NOT NULL,
    age INTEGER NOT NULL,
    size VARCHAR(50) NOT NULL,
    personality VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    shelter_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pets_status ON pets(status);
CREATE INDEX idx_pets_created_at ON pets(created_at);
CREATE INDEX idx_pets_name ON pets(name);
CREATE INDEX idx_pets_species ON pets(species);
