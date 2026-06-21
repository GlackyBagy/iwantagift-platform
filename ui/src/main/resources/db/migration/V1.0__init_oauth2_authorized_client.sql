-- Schema for Spring Security's JdbcOAuth2AuthorizedClientService.
-- Mirrors org/springframework/security/oauth2/client/oauth2-client-schema.sql,
-- adapted to PostgreSQL (BYTEA for token values instead of BLOB).
CREATE TABLE oauth2_authorized_client
(
    client_registration_id  VARCHAR(100)                            NOT NULL,
    principal_name          VARCHAR(200)                            NOT NULL,
    access_token_type       VARCHAR(100)                            NOT NULL,
    access_token_value      BYTEA                                   NOT NULL,
    access_token_issued_at  TIMESTAMP                               NOT NULL,
    access_token_expires_at TIMESTAMP                               NOT NULL,
    access_token_scopes     VARCHAR(1000) DEFAULT NULL,
    refresh_token_value     BYTEA         DEFAULT NULL,
    refresh_token_issued_at TIMESTAMP     DEFAULT NULL,
    created_at              TIMESTAMP     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (client_registration_id, principal_name)
);
