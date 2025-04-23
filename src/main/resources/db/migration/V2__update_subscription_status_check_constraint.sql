ALTER TABLE subscriptions DROP CONSTRAINT IF EXISTS subscriptions_status_check;

ALTER TABLE subscriptions ADD CONSTRAINT subscriptions_status_check 
CHECK (status IN ('PAYMENT_PENDING', 'ACTIVE', 'CANCELLED', 'EXPIRED')); 