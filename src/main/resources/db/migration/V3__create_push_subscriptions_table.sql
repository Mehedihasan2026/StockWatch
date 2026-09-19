CREATE TABLE push_subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    endpoint VARCHAR(512) NOT NULL,
    p256dh VARCHAR(512) NOT NULL,
    auth VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uk_push_subscriptions_endpoint (endpoint)
);
