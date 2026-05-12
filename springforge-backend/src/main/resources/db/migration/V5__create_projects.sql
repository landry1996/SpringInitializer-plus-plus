CREATE TABLE springforge.projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    group_id VARCHAR(255) NOT NULL,
    artifact_id VARCHAR(255) NOT NULL,
    description TEXT,
    config_json TEXT NOT NULL,
    owner_id UUID NOT NULL REFERENCES springforge.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_projects_owner_id ON springforge.projects(owner_id);
CREATE INDEX idx_projects_status ON springforge.projects(status);
CREATE INDEX idx_projects_artifact_id ON springforge.projects(artifact_id);
