-- V7__add_article_source_fields.sql
-- Adds source tracking, CSDN article ID, and content hash to articles table

ALTER TABLE articles ADD COLUMN source VARCHAR(50) NULL COMMENT 'Article source: CSDN, MANUAL, etc.';
ALTER TABLE articles ADD COLUMN csdn_article_id VARCHAR(100) NULL COMMENT 'CSDN article ID for deduplication';
ALTER TABLE articles ADD UNIQUE INDEX idx_csdn_article_id (csdn_article_id);
ALTER TABLE articles ADD COLUMN content_hash VARCHAR(32) NULL COMMENT 'MD5 hash of CSDN content for update detection (D-03)';