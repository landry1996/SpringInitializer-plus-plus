CREATE TABLE presets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    configuration JSONB NOT NULL,
    owner_id UUID NOT NULL REFERENCES users(id),
    is_shared BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_presets_owner_id ON presets(owner_id);
CREATE INDEX idx_presets_shared ON presets(is_shared) WHERE is_shared = TRUE;
