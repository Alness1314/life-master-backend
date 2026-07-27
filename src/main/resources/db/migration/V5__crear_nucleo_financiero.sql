CREATE TABLE financial_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(128) NOT NULL,
    account_type VARCHAR(32) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    initial_balance NUMERIC(21,8) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    erased BOOLEAN NOT NULL DEFAULT FALSE,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX uq_financial_account_name
    ON financial_accounts(user_id, LOWER(name)) WHERE erased = FALSE;

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    account_id UUID NULL REFERENCES financial_accounts(id),
    name VARCHAR(128) NOT NULL,
    method_type VARCHAR(32) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    erased BOOLEAN NOT NULL DEFAULT FALSE,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
CREATE UNIQUE INDEX uq_payment_method_name
    ON payment_methods(user_id, LOWER(name)) WHERE erased = FALSE;

CREATE TABLE budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NULL REFERENCES categories(id),
    budget_year INTEGER NOT NULL,
    budget_month INTEGER NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'MXN',
    amount NUMERIC(21,8) NOT NULL,
    alert_percentage INTEGER NOT NULL DEFAULT 80,
    erased BOOLEAN NOT NULL DEFAULT FALSE,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT ck_budget_month CHECK (budget_month BETWEEN 1 AND 12),
    CONSTRAINT ck_budget_amount CHECK (amount > 0),
    CONSTRAINT ck_budget_alert CHECK (alert_percentage BETWEEN 1 AND 100)
);
CREATE UNIQUE INDEX uq_budget_period_category_currency
    ON budgets (user_id, budget_year, budget_month, currency,
        COALESCE(category_id, '00000000-0000-0000-0000-000000000000'::uuid))
    WHERE erased = FALSE;

CREATE TABLE recurring_movements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    category_id UUID NULL REFERENCES categories(id),
    account_id UUID NULL REFERENCES financial_accounts(id),
    payment_method_id UUID NULL REFERENCES payment_methods(id),
    movement_type VARCHAR(16) NOT NULL,
    description VARCHAR(256) NOT NULL,
    amount NUMERIC(21,8) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    frequency VARCHAR(16) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NULL,
    next_execution_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    erased BOOLEAN NOT NULL DEFAULT FALSE,
    create_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    update_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT ck_recurring_amount CHECK (amount > 0),
    CONSTRAINT ck_recurring_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

ALTER TABLE expenses ADD COLUMN account_id UUID NULL REFERENCES financial_accounts(id);
ALTER TABLE expenses ADD COLUMN payment_method_id UUID NULL REFERENCES payment_methods(id);
ALTER TABLE expenses ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'MXN';
ALTER TABLE income ADD COLUMN account_id UUID NULL REFERENCES financial_accounts(id);
ALTER TABLE income ADD COLUMN currency VARCHAR(3) NOT NULL DEFAULT 'MXN';
ALTER TABLE payments ADD COLUMN payment_method_id UUID NULL REFERENCES payment_methods(id);

CREATE INDEX idx_expenses_user_date ON expenses(user_id, payment_date) WHERE erased = FALSE;
CREATE INDEX idx_income_user_date ON income(user_id, payment_date) WHERE erased = FALSE;
CREATE INDEX idx_debts_user ON debts(user_id) WHERE erased = FALSE;
