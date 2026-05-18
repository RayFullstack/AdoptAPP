CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    donor_name VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    user_id BIGINT NOT NULL,
    shelter_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE donation_history (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20),
    previous_amount DECIMAL(12,2),
    new_amount DECIMAL(12,2),
    comment VARCHAR(255),
    changed_by_user_id BIGINT,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donation_history_donation_id ON donation_history(donation_id);
