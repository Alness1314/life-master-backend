CREATE TABLE stored_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    original_name VARCHAR(256) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL CHECK (size_bytes >= 0),
    storage_path VARCHAR(512) NOT NULL UNIQUE,
    sha256 VARCHAR(64) NOT NULL,
    purpose VARCHAR(32) NOT NULL,
    erased BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

CREATE INDEX idx_stored_files_user_created
    ON stored_files(user_id, created_at DESC)
    WHERE erased = FALSE;

ALTER TABLE audit_events ADD COLUMN action VARCHAR(32);
ALTER TABLE audit_events ADD COLUMN module VARCHAR(64);
ALTER TABLE audit_events ADD COLUMN record_id VARCHAR(64);
ALTER TABLE audit_events ADD COLUMN detail VARCHAR(1024);
ALTER TABLE audit_events ADD COLUMN successful BOOLEAN;
ALTER TABLE audit_events ADD COLUMN user_agent VARCHAR(512);

UPDATE audit_events
SET action = CASE
        WHEN method = 'POST' THEN 'CREAR'
        WHEN method IN ('PUT', 'PATCH') THEN 'ACTUALIZAR'
        WHEN method = 'DELETE' THEN 'ELIMINAR'
        ELSE 'CONSULTAR'
    END,
    module = 'SISTEMA',
    detail = method || ' ' || resource,
    successful = response_status < 400
WHERE action IS NULL;

ALTER TABLE audit_events ALTER COLUMN action SET NOT NULL;
ALTER TABLE audit_events ALTER COLUMN module SET NOT NULL;
ALTER TABLE audit_events ALTER COLUMN successful SET NOT NULL;

CREATE INDEX idx_audit_events_module_date ON audit_events(module, created_at DESC);
CREATE INDEX idx_audit_events_action_date ON audit_events(action, created_at DESC);
CREATE INDEX idx_audit_events_record ON audit_events(record_id) WHERE record_id IS NOT NULL;
CREATE INDEX idx_audit_events_result_date ON audit_events(successful, created_at DESC);
