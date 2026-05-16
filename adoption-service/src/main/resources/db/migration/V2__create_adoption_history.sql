CREATE TABLE adoption_history (
    id BIGSERIAL PRIMARY KEY,
    adoption_id BIGINT NOT NULL REFERENCES adoptions(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_adoption_history_adoption_id ON adoption_history(adoption_id);
CREATE INDEX idx_adoption_history_action ON adoption_history(action);
CREATE INDEX idx_adoption_history_created_at ON adoption_history(created_at);
