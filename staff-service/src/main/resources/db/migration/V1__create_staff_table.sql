CREATE TABLE staff (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    shelter_id BIGINT NOT NULL,
    position VARCHAR(20) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    hire_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE staff_history (
    id BIGSERIAL PRIMARY KEY,
    staff_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_position VARCHAR(20),
    new_position VARCHAR(20),
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_phone VARCHAR(20),
    new_phone VARCHAR(20),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_staff_history_staff FOREIGN KEY (staff_id) REFERENCES staff(id) ON DELETE CASCADE
);

CREATE INDEX idx_staff_status ON staff(status);
CREATE INDEX idx_staff_shelter_id ON staff(shelter_id);
CREATE INDEX idx_staff_history_staff_id ON staff_history(staff_id);
