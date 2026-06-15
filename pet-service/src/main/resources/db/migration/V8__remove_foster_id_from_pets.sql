DROP INDEX IF EXISTS idx_pets_foster_id;

ALTER TABLE pets
    DROP COLUMN IF EXISTS foster_id;

ALTER TABLE pet_history
    DROP COLUMN IF EXISTS previous_foster_id,
    DROP COLUMN IF EXISTS new_foster_id;

ALTER TABLE pets
    ALTER COLUMN shelter_id SET NOT NULL;
