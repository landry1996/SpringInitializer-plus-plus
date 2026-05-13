CREATE TABLE IF NOT EXISTS springforge.subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    plan VARCHAR(20) NOT NULL DEFAULT 'FREE',
    stripe_customer_id VARCHAR(255),
    stripe_subscription_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    current_period_start TIMESTAMP,
    current_period_end TIMESTAMP,
    canceled_at TIMESTAMP,
    trial BOOLEAN NOT NULL DEFAULT FALSE,
    trial_ends_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user_id ON springforge.subscriptions(user_id);
CREATE INDEX idx_subscriptions_stripe_customer ON springforge.subscriptions(stripe_customer_id);
CREATE INDEX idx_subscriptions_trial ON springforge.subscriptions(trial, trial_ends_at);

CREATE TABLE IF NOT EXISTS springforge.invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    stripe_invoice_id VARCHAR(255) UNIQUE,
    amount_cents INTEGER NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'eur',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    invoice_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_invoices_user_id ON springforge.invoices(user_id);
