CREATE TABLE stocks (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        ticker VARCHAR(20) NOT NULL,
                        company_name VARCHAR(255) NOT NULL,
                        shares INT NOT NULL,
                        buy_price DECIMAL(12, 4) NOT NULL,
                        currency VARCHAR(3) NOT NULL,
                        target_price DECIMAL(12, 4) NOT NULL,
                        notes TEXT,
                        alert_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                        alert_triggered BOOLEAN NOT NULL DEFAULT FALSE,
                        created_at DATETIME NOT NULL,
                        updated_at DATETIME NOT NULL,

                        PRIMARY KEY (id),
                        UNIQUE KEY uk_stocks_ticker (ticker),

                        CONSTRAINT chk_stocks_shares_positive CHECK (shares > 0),
                        CONSTRAINT chk_stocks_buy_price_positive CHECK (buy_price > 0),
                        CONSTRAINT chk_stocks_target_price_positive CHECK (target_price > 0),
                        CONSTRAINT chk_stocks_currency CHECK (currency IN ('USD', 'EUR', 'DKK'))
);