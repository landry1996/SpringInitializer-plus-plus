CREATE TABLE IF NOT EXISTS springforge.webhook_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    url VARCHAR(500) NOT NULL,
    secret_token VARCHAR(255),
    channel VARCHAR(20) NOT NULL DEFAULT 'WEBHOOK',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_webhook_configs_org ON springforge.webhook_configs(organization_id);

CREATE TABLE IF NOT EXISTS springforge.webhook_events (
    webhook_config_id UUID NOT NULL REFERENCES springforge.webhook_configs(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    PRIMARY KEY (webhook_config_id, event_type)
);

CREATE TABLE IF NOT EXISTS springforge.delivery_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    webhook_config_id UUID NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT,
    http_status INTEGER DEFAULT 0,
    response_body TEXT,
    success BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_logs_webhook ON springforge.delivery_logs(webhook_config_id);
CREATE INDEX idx_delivery_logs_retry ON springforge.delivery_logs(success, attempt_count, next_retry_at);
