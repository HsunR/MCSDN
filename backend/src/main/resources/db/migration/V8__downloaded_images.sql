-- V8__downloaded_images.sql
-- Tracks downloaded CSDN images for MD5-based deduplication

CREATE TABLE downloaded_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url_hash VARCHAR(32) NOT NULL UNIQUE COMMENT 'MD5 hash of original URL',
    local_path VARCHAR(500) NOT NULL COMMENT 'Path relative to upload.path',
    original_url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_url_hash (url_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
