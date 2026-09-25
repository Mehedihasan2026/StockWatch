CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,

                       email VARCHAR(255) NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       display_name VARCHAR(100) NOT NULL,

                       enabled BOOLEAN NOT NULL DEFAULT TRUE,

                       created_at DATETIME NOT NULL,
                       updated_at DATETIME NOT NULL,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_email (email)
);