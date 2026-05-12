CREATE TABLE admin_users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    action VARCHAR(100) NOT NULL,
    details VARCHAR(2000),
    ip_address VARCHAR(45),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE generation_stats (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    architecture_type VARCHAR(50),
    java_version VARCHAR(10),
    spring_boot_version VARCHAR(20),
    build_tool VARCHAR(20),
    dependency_count INT DEFAULT 0,
    generation_time_ms BIGINT DEFAULT 0,
    success BOOLEAN DEFAULT TRUE,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp DESC);
CREATE INDEX idx_generation_stats_user ON generation_stats(user_id);
CREATE INDEX idx_generation_stats_date ON generation_stats(generated_at DESC);
CREATE INDEX idx_generation_stats_arch ON generation_stats(architecture_type);
