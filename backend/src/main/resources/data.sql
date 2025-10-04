-- Create initial roles
INSERT INTO role (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with all permissions'),
(2, 'ROLE_SUB_ADMIN', 'Sub Administrator with limited permissions'),
(3, 'ROLE_EDITOR', 'Editor with document editing permissions'),
(4, 'ROLE_USER', 'Regular user with viewing permissions')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Create initial permissions
INSERT INTO permission (id, code, description) VALUES
(1, 'DOC:CREATE', 'Create documents'),
(2, 'DOC:EDIT', 'Edit documents'),
(3, 'DOC:PUBLISH', 'Publish documents'),
(4, 'DOC:DELETE', 'Delete documents'),
(5, 'DOC:APPROVE:ALL', 'Approve all documents'),
(6, 'DOC:APPROVE:ASSIGNED', 'Approve assigned documents'),
(7, 'DOC:VIEW:LOGGED', 'View documents when logged in'),
(8, 'DOC:DOWNLOAD', 'Download documents'),
(9, 'DOC:ASSIGN', 'Assign users to documents'),
(10, 'COMMENT:CREATE', 'Create comments'),
(11, 'COMMENT:MANAGE', 'Manage comments'),
(12, 'USER:MANAGE:SUB', 'Manage sub-admins and editors'),
(13, 'USER:MANAGE:EDITOR', 'Manage editors'),
(14, 'USER:READ', 'Read user information')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Link roles to permissions
-- Admin gets all permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT 1, id FROM permission
ON DUPLICATE KEY UPDATE permission_id = permission_id;

-- Sub-Admin permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT 2, id FROM permission WHERE code IN (
    'DOC:CREATE', 'DOC:EDIT', 'DOC:PUBLISH', 'DOC:APPROVE:ASSIGNED',
    'DOC:VIEW:LOGGED', 'DOC:DOWNLOAD', 'DOC:ASSIGN', 'COMMENT:CREATE',
    'COMMENT:MANAGE', 'USER:MANAGE:EDITOR', 'USER:READ'
)
ON DUPLICATE KEY UPDATE permission_id = permission_id;

-- Editor permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT 3, id FROM permission WHERE code IN (
    'DOC:EDIT', 'DOC:PUBLISH', 'DOC:VIEW:LOGGED', 'DOC:DOWNLOAD',
    'COMMENT:CREATE', 'COMMENT:MANAGE', 'USER:READ'
)
ON DUPLICATE KEY UPDATE permission_id = permission_id;

-- User permissions
INSERT INTO role_permission (role_id, permission_id)
SELECT 4, id FROM permission WHERE code IN (
    'DOC:VIEW:LOGGED', 'DOC:DOWNLOAD', 'COMMENT:CREATE', 'USER:READ'
)
ON DUPLICATE KEY UPDATE permission_id = permission_id;

-- Create initial admin user (password: admin123, encoded with BCrypt)
-- The password "admin123" when encoded with BCrypt: $2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uKw4Ky
INSERT INTO users (id, username, email, password, status) VALUES
(1, 'admin', 'admin@cms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uKw4Ky', 'ACTIVE')
ON DUPLICATE KEY UPDATE email = VALUES(email), password = VALUES(password), status = VALUES(status);

-- Assign admin role to admin user
INSERT INTO user_role (user_id, role_id) VALUES
(1, 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- Create test users for different roles
INSERT INTO users (id, username, email, password, status) VALUES
(2, 'subadmin', 'subadmin@cms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uKw4Ky', 'ACTIVE'),
(3, 'editor', 'editor@cms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uKw4Ky', 'ACTIVE'),
(4, 'user', 'user@cms.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl.k5uKw4Ky', 'ACTIVE')
ON DUPLICATE KEY UPDATE email = VALUES(email), password = VALUES(password), status = VALUES(status);

-- Assign roles to test users
INSERT INTO user_role (user_id, role_id) VALUES
(2, 2), -- subadmin
(3, 3), -- editor
(4, 4)  -- user
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);