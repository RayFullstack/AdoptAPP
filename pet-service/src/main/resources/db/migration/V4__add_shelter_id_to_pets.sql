ALTER TABLE pets ADD COLUMN shelter_id BIGINT;

UPDATE pets SET shelter_id = foster_id WHERE shelter_id IS NULL;
