CREATE TABLE expense_receipts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expense_id UUID NOT NULL REFERENCES expenses(id),
    user_id UUID NOT NULL REFERENCES users(id),
    original_name VARCHAR(256) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    content BYTEA NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX idx_expense_receipts_expense ON expense_receipts(expense_id);

CREATE TABLE bank_imports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    file_name VARCHAR(256) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    imported_rows INTEGER NOT NULL,
    imported_expenses INTEGER NOT NULL,
    imported_income INTEGER NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uq_bank_import_hash UNIQUE(user_id, file_hash)
);

CREATE TABLE financial_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    alert_type VARCHAR(32) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    title VARCHAR(256) NOT NULL,
    message VARCHAR(1024) NOT NULL,
    reference_key VARCHAR(256) NOT NULL,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uq_financial_alert_reference UNIQUE(user_id, reference_key)
);

CREATE TABLE financial_reminders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(256) NOT NULL,
    message VARCHAR(1024) NOT NULL,
    scheduled_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    delivered BOOLEAN NOT NULL DEFAULT FALSE,
    cancelled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX idx_financial_reminders_due
    ON financial_reminders(scheduled_at) WHERE delivered = FALSE AND cancelled = FALSE;

CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NULL,
    username VARCHAR(256) NULL,
    method VARCHAR(8) NOT NULL,
    resource VARCHAR(512) NOT NULL,
    response_status INTEGER NOT NULL,
    correlation_id VARCHAR(64) NOT NULL,
    ip_address VARCHAR(64) NULL,
    duration_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE INDEX idx_audit_events_user_date ON audit_events(user_id, created_at DESC);
