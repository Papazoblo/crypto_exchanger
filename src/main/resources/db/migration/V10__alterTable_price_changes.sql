ALTER TABLE price_changes
ADD COLUMN type VARCHAR(10);

UPDATE price_changes
SET type = 'SHORT'
WHERE id IS NOT NULL;