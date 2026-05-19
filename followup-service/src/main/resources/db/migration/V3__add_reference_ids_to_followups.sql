ALTER TABLE followups ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE followups ADD COLUMN IF NOT EXISTS pet_id BIGINT;
ALTER TABLE followups ADD COLUMN IF NOT EXISTS adoption_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_followups_user_id ON followups(user_id);
CREATE INDEX IF NOT EXISTS idx_followups_pet_id ON followups(pet_id);
CREATE INDEX IF NOT EXISTS idx_followups_adoption_id ON followups(adoption_id);
