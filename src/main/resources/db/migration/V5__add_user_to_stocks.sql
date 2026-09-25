ALTER TABLE stocks
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE stocks
DROP INDEX uk_stocks_ticker;

ALTER TABLE stocks
    ADD CONSTRAINT fk_stocks_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE;

ALTER TABLE stocks
    ADD UNIQUE KEY uk_stocks_user_ticker (
    user_id,
    ticker
    );