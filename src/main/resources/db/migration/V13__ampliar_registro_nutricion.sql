ALTER TABLE nutrition ADD COLUMN name VARCHAR(256);

UPDATE nutrition
SET name = COALESCE(NULLIF(TRIM(meal_type), ''), 'Comida')
WHERE name IS NULL;

ALTER TABLE nutrition ALTER COLUMN name SET NOT NULL;

ALTER TABLE nutrition ADD COLUMN photo_file_id UUID UNIQUE;
ALTER TABLE nutrition
    ADD CONSTRAINT fk_nutrition_photo_file
    FOREIGN KEY (photo_file_id) REFERENCES stored_files(id);

CREATE INDEX idx_nutrition_photo_file ON nutrition(photo_file_id)
WHERE photo_file_id IS NOT NULL;
