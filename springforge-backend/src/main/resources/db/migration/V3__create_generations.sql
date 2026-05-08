CREATE TABLE springforge.generations (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL,
    user_id UUID NOT NULL REFERENCES springforge.users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'QUEUED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    output_path VARCHAR(500),
    error_message VARCHAR(1000),
    configuration_json TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_generations_user_id ON springforge.generations(user_id);
CREATE INDEX idx_generations_status ON springforge.generations(status);
