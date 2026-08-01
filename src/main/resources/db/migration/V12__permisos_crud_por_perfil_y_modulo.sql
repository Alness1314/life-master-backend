ALTER TABLE modules
    ADD COLUMN permission_key VARCHAR(64);

UPDATE modules
SET permission_key = NULLIF(
        REGEXP_REPLACE(LOWER(TRIM(route)), '^/+|/+$', '', 'g'),
        '')
WHERE permission_key IS NULL;

CREATE INDEX idx_modules_permission_key
    ON modules (permission_key)
    WHERE erased = FALSE AND permission_key IS NOT NULL;

ALTER TABLE profile_modules
    ADD CONSTRAINT uq_profile_modules_profile_module UNIQUE (profile_id, module_id);

CREATE TABLE permissions (
    profile_id UUID NOT NULL,
    module_id UUID NOT NULL,
    can_create BOOLEAN NOT NULL DEFAULT FALSE,
    can_read BOOLEAN NOT NULL DEFAULT FALSE,
    can_update BOOLEAN NOT NULL DEFAULT FALSE,
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT permissions_pkey PRIMARY KEY (profile_id, module_id),
    CONSTRAINT fk_permissions_profile
        FOREIGN KEY (profile_id) REFERENCES profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_permissions_module
        FOREIGN KEY (module_id) REFERENCES modules(id) ON DELETE CASCADE,
    CONSTRAINT fk_permissions_profile_module
        FOREIGN KEY (profile_id, module_id)
        REFERENCES profile_modules(profile_id, module_id) ON DELETE CASCADE
);

-- Conserva el acceso que ya existía mediante profile_modules. A partir de esta
-- migración, los cuatro indicadores pasan a ser la fuente de autorización.
INSERT INTO permissions (
    profile_id, module_id, can_create, can_read, can_update, can_delete)
SELECT profile_id, module_id, TRUE, TRUE, TRUE, TRUE
FROM profile_modules
ON CONFLICT (profile_id, module_id) DO NOTHING;

CREATE INDEX idx_permissions_profile ON permissions(profile_id);
CREATE INDEX idx_permissions_module ON permissions(module_id);
