CREATE TABLE IF NOT EXISTS accounts
(
    id            UUID,
    nickname      VARCHAR(64) NOT NULL,
    password_hash TEXT        NOT NULL,
    email         VARCHAR     NOT NULL UNIQUE,
    CONSTRAINT pk_accounts PRIMARY KEY (id)
);