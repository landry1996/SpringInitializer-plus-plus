CREATE TABLE springforge.templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(500) NOT NULL UNIQUE,
    category VARCHAR(100) NOT NULL,
    scope VARCHAR(20) NOT NULL DEFAULT 'CORE',
    content TEXT NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    blueprint_type VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_templates_category ON springforge.templates(category);
CREATE INDEX idx_templates_blueprint_type ON springforge.templates(blueprint_type);
CREATE INDEX idx_templates_path ON springforge.templates(path);
