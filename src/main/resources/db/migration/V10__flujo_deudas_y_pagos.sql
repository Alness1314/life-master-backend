ALTER TABLE debts
    ADD COLUMN disburses_funds BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN received_amount NUMERIC(21,8),
    ADD COLUMN received_date DATE,
    ADD COLUMN deposit_account_id UUID REFERENCES financial_accounts(id);

ALTER TABLE payments
    ADD COLUMN principal_amount NUMERIC(21,8),
    ADD COLUMN interest_amount NUMERIC(21,8) NOT NULL DEFAULT 0,
    ADD COLUMN account_id UUID REFERENCES financial_accounts(id);

UPDATE payments
SET principal_amount = amount_paid
WHERE principal_amount IS NULL;

ALTER TABLE payments
    ALTER COLUMN principal_amount SET NOT NULL;

ALTER TABLE debts
    ADD CONSTRAINT ck_debt_disbursement
    CHECK (
        (disburses_funds = FALSE
            AND received_amount IS NULL
            AND received_date IS NULL
            AND deposit_account_id IS NULL)
        OR
        (disburses_funds = TRUE
            AND received_amount > 0
            AND received_date IS NOT NULL
            AND deposit_account_id IS NOT NULL)
    );

ALTER TABLE payments
    ADD CONSTRAINT ck_payment_breakdown
    CHECK (
        principal_amount > 0
        AND interest_amount >= 0
        AND amount_paid = principal_amount + interest_amount
    );

CREATE INDEX idx_debts_received_date
    ON debts(user_id, received_date)
    WHERE erased = FALSE AND disburses_funds = TRUE;

CREATE INDEX idx_payments_account_date
    ON payments(account_id, payment_date)
    WHERE is_paid = TRUE;
