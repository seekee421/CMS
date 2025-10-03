-- Insert admin user with encoded password for "admin123"
-- Using BCrypt encoded password: $2a$10$9oZ81mRz6Yy3.k6VQb3FPeF6j6qG2q6o2W8q6mK7s5Y7s3s5Y7s3.
-- This is an example hash, you should generate a real one
INSERT INTO users (id, username, password, email, status) VALUES
(1, 'admin', '$2a$10$9oZ81mRz6Yy3.k6VQb3FPeF6j6qG2q6o2W8q6mK7s5Y7s3s5Y7s3.', 'admin@cms.com', 'ACTIVE')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- Assign admin role to admin user
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Insert a test document
INSERT INTO document (id, title, content, status, is_public, created_by, created_at, updated_at) VALUES
(1, 'Welcome to CMS', 'This is a sample document to demonstrate the CMS functionality.', 'PUBLISHED', true, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE title = VALUES(title);