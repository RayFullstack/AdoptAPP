CREATE TABLE pet_history (
    id BIGSERIAL PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    previous_name VARCHAR(20),
    new_name VARCHAR(20),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_foster_id BIGINT,
    new_foster_id BIGINT,
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment VARCHAR(255),
    CONSTRAINT fk_pet_history_pet FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

CREATE INDEX idx_pet_history_pet_id ON pet_history(pet_id);
CREATE INDEX idx_pet_history_changed_at ON pet_history(changed_at);
