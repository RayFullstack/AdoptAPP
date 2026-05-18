CREATE TABLE health (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    sterilization_status VARCHAR(50) NOT NULL,
    vaccination_status VARCHAR(50) NOT NULL,
    diseases TEXT DEFAULT 'Ninguna' NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE health_history (
    id BIGSERIAL PRIMARY KEY,
    health_id BIGINT NOT NULL,
    previous_sterilization_status VARCHAR(150),
    new_sterilization_status VARCHAR(150),
    previous_vaccination_status VARCHAR(150),
    new_vaccination_status VARCHAR(150),
    previous_disease VARCHAR(150),
    new_disease VARCHAR(150),
    action VARCHAR(50) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    CONSTRAINT fk_health_history_health FOREIGN KEY (health_id) REFERENCES health(id)
);
