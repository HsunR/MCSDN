ALTER TABLE tags ADD COLUMN slug VARCHAR(255) UNIQUE;
UPDATE tags SET slug = LOWER(REPLACE(REGEXP_REPLACE(name, '[^a-zA-Z0-9\s-]', ''), ' ', '-'));
