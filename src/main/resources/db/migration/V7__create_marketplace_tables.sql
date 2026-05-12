CREATE TABLE blueprints (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    author VARCHAR(255) NOT NULL,
    version VARCHAR(50) NOT NULL DEFAULT '1.0.0',
    category VARCHAR(50) NOT NULL,
    configuration_json TEXT,
    downloads INT DEFAULT 0,
    rating DOUBLE DEFAULT 0,
    rating_count INT DEFAULT 0,
    published BOOLEAN DEFAULT FALSE,
    verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE blueprint_tags (
    blueprint_id VARCHAR(36) NOT NULL,
    tag VARCHAR(100) NOT NULL,
    FOREIGN KEY (blueprint_id) REFERENCES blueprints(id) ON DELETE CASCADE
);

CREATE TABLE blueprint_comments (
    id VARCHAR(36) PRIMARY KEY,
    blueprint_id VARCHAR(36) NOT NULL,
    author VARCHAR(255) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (blueprint_id) REFERENCES blueprints(id) ON DELETE CASCADE
);

CREATE INDEX idx_blueprints_category ON blueprints(category);
CREATE INDEX idx_blueprints_published ON blueprints(published);
CREATE INDEX idx_blueprints_author ON blueprints(author);
CREATE INDEX idx_blueprints_rating ON blueprints(rating DESC);
CREATE INDEX idx_blueprints_downloads ON blueprints(downloads DESC);
CREATE INDEX idx_blueprint_tags_tag ON blueprint_tags(tag);
CREATE INDEX idx_blueprint_comments_blueprint ON blueprint_comments(blueprint_id);
