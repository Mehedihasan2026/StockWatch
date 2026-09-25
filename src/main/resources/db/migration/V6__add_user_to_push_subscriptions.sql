ALTER TABLE push_subscriptions
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE push_subscriptions
    ADD CONSTRAINT fk_push_subscriptions_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

CREATE INDEX idx_push_subscriptions_user
    ON push_subscriptions(user_id);