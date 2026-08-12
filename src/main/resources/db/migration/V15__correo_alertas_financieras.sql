ALTER TABLE financial_alerts
    ADD COLUMN email_sent_at TIMESTAMP WITHOUT TIME ZONE NULL;

-- Las alertas históricas ya fueron mostradas dentro de la aplicación. Se marcan
-- como procesadas para no enviar una ráfaga de correos al aplicar esta migración.
UPDATE financial_alerts
SET email_sent_at = created_at
WHERE email_sent_at IS NULL;
