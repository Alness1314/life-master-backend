CREATE INDEX IF NOT EXISTS idx_assistance_user_work_date
    ON assistance(user_id, work_date);

CREATE INDEX IF NOT EXISTS idx_exercises_user_training_date
    ON exercises(user_id, training_date)
    WHERE erased = FALSE;

CREATE INDEX IF NOT EXISTS idx_nutrition_user_consumption_date
    ON nutrition(user_id, date_time_consumption)
    WHERE erased = FALSE;

CREATE INDEX IF NOT EXISTS idx_payments_debt_payment_date
    ON payments(debts_id, payment_date);
