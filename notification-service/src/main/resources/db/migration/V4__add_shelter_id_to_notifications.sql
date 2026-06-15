ALTER TABLE notifications
    ADD COLUMN shelter_id BIGINT;

CREATE INDEX idx_notifications_shelter_id ON notifications(shelter_id);
