ALTER TABLE payments
DROP COLUMN payment_date;

ALTER TABLE debts
DROP COLUMN "description";

ALTER TABLE payments
ADD payment_date date;

ALTER TABLE payments
ADD is_paid boolean;