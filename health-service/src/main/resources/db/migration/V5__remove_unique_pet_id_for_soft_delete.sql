ALTER TABLE health DROP CONSTRAINT IF EXISTS uq_health_pet_id;

CREATE INDEX IF NOT EXISTS idx_health_pet_id ON health (pet_id);
