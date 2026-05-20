ALTER TABLE donation_history
    ADD CONSTRAINT fk_donation_history_donation
    FOREIGN KEY (donation_id) REFERENCES donations(id)
    ON DELETE CASCADE;
