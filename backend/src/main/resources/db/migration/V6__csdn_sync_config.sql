-- V6__csdn_sync_config.sql
-- Creates CSDN sync configuration table

CREATE TABLE csdn_sync_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    csdn_user_id VARCHAR(100) NOT NULL,
    category_id BIGINT,
    enabled BOOLEAN DEFAULT TRUE,
    last_sync_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default config (disabled until user configures)
INSERT INTO csdn_sync_config (csdn_user_id, enabled) VALUES ('', FALSE);