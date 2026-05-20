CREATE TABLE followups (
    id BIGSERIAL PRIMARY KEY,
    adopter_name VARCHAR(100) NOT NULL,
    pet_name VARCHAR(100) NOT NULL,
    visit_date TIMESTAMP NOT NULL,
    comments TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE followup_history (
    id BIGSERIAL PRIMARY KEY,
    followup_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_followup_history_followup FOREIGN KEY (followup_id) REFERENCES followups(id) ON DELETE CASCADE
);

CREATE INDEX idx_followups_status ON followups(status);
CREATE INDEX idx_followup_history_followup_id ON followup_history(followup_id);
