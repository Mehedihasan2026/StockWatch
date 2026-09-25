DELETE FROM push_subscriptions
WHERE user_id IS NULL;

ALTER TABLE push_subscriptions
    MODIFY COLUMN user_id BIGINT NOT NULL;