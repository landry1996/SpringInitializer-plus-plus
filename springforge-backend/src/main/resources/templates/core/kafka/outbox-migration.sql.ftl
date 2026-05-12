CREATE TABLE outbox_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    published_at TIMESTAMP WITH TIME ZONE,
    published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_outbox_unpublished ON outbox_events(published, created_at) WHERE published = FALSE;
CREATE INDEX idx_outbox_cleanup ON outbox_events(published, published_at) WHERE published = TRUE;
