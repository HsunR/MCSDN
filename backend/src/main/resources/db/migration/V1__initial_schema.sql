-- V1__initial_schema.sql
-- Creates admin_users table for authentication

CREATE TABLE admin_users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(60) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user
-- Password: admin123 (BCrypt hash generated via BCryptPasswordEncoder)
INSERT INTO admin_users (username, password) VALUES
  ('admin', '$2a$10$Pty8awCO0VJwlMHp.n3ZN.gaP3NG0F6xsDllF/gH8NP/5MFqJLNx.');