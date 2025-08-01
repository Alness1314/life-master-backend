-- Tabla: debts
CREATE TABLE debts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    creditor_name VARCHAR(256) NOT NULL,
    total_amount NUMERIC(21,8) NOT NULL,
    currency VARCHAR(128) NOT NULL,
    has_interest BOOLEAN NOT NULL,
    number_of_payments BIGINT NOT NULL,
    payments_made BIGINT NOT NULL,
    due_date DATE NOT NULL,
    "description" TEXT,
    is_fully_paid BOOLEAN NOT NULL,
    notes TEXT,
    user_id UUID NOT NULL,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    erased BOOLEAN NOT NULL,
    CONSTRAINT fk_debts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Tabla: payments
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    amount_paid NUMERIC(21,8) NOT NULL,
    payment_method VARCHAR(256),
    notes TEXT,
    debts_id UUID,
    CONSTRAINT fk_payments_debts FOREIGN KEY (debts_id) REFERENCES debts(id) ON DELETE CASCADE
);