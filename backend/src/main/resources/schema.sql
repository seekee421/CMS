-- This script will be executed to create tables if they don't exist
-- Hibernate will handle the table creation based on entities,
-- but we define the basic structure here for reference

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT,
    role_id BIGINT,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS role_permission (
    role_id BIGINT,
    permission_id BIGINT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permission(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    status VARCHAR(20) DEFAULT 'DRAFT',
    is_public BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS doc_assignment (
    doc_id BIGINT,
    user_id BIGINT,
    assignment_type VARCHAR(20) NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    PRIMARY KEY (doc_id, user_id),
    FOREIGN KEY (doc_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(1000) NOT NULL,
    doc_id BIGINT,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (doc_id) REFERENCES document(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 文档备份表
CREATE TABLE IF NOT EXISTS document_backup (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT NOT NULL,
    backup_version VARCHAR(50) NOT NULL,
    title VARCHAR(255),
    content LONGTEXT,
    status VARCHAR(20),
    is_public BOOLEAN,
    created_by BIGINT,
    original_created_at TIMESTAMP,
    original_updated_at TIMESTAMP,
    backup_created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    backup_type VARCHAR(20) NOT NULL,
    backup_reason VARCHAR(500),
    file_path VARCHAR(1000),
    file_size BIGINT,
    checksum VARCHAR(64),
    backup_status VARCHAR(20) DEFAULT 'PENDING',
    INDEX idx_document_backup_doc_id (document_id),
    INDEX idx_document_backup_version (document_id, backup_version),
    INDEX idx_document_backup_created_at (backup_created_at),
    INDEX idx_document_backup_status (backup_status),
    FOREIGN KEY (document_id) REFERENCES document(id) ON DELETE CASCADE
);

-- 备份配置表
CREATE TABLE IF NOT EXISTS backup_configuration (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_name VARCHAR(100) UNIQUE NOT NULL,
    auto_backup_enabled BOOLEAN DEFAULT TRUE,
    backup_interval_hours INT DEFAULT 24,
    max_backup_versions INT DEFAULT 10,
    backup_retention_days INT DEFAULT 30,
    backup_storage_path VARCHAR(500),
    compression_enabled BOOLEAN DEFAULT TRUE,
    encryption_enabled BOOLEAN DEFAULT FALSE,
    backup_on_update BOOLEAN DEFAULT TRUE,
    backup_on_delete BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);