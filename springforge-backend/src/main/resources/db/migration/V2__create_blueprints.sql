CREATE TABLE springforge.blueprints (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    constraints_json TEXT,
    defaults_json TEXT,
    structure_json TEXT,
    version INTEGER NOT NULL DEFAULT 1,
    built_in BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_blueprints_type ON springforge.blueprints(type);
