CREATE TABLE email_verification_tokens
(
    id             UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    token_hash     VARCHAR(255)                NOT NULL,
    expires_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    used_at        TIMESTAMP(6) WITH TIME ZONE,
    invalidated_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_email_verification_tokens PRIMARY KEY (id)
);

CREATE TABLE oauth_accounts
(
    id                      UUID                        NOT NULL,
    user_id                 UUID                        NOT NULL,
    provider                VARCHAR(255)                NOT NULL,
    provider_user_id        VARCHAR(255)                NOT NULL,
    provider_email          VARCHAR(255),
    provider_email_verified BOOLEAN,
    linked_at               TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_oauth_accounts PRIMARY KEY (id)
);

CREATE TABLE password_reset_tokens
(
    id             UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    token_hash     VARCHAR(255)                NOT NULL,
    expires_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    used_at        TIMESTAMP(6) WITH TIME ZONE,
    invalidated_at TIMESTAMP(6) WITH TIME ZONE,
    CONSTRAINT pk_password_reset_tokens PRIMARY KEY (id)
);

CREATE TABLE refresh_tokens
(
    id                   UUID                        NOT NULL,
    user_id              UUID                        NOT NULL,
    session_id           UUID                        NOT NULL,
    token_hash           VARCHAR(255)                NOT NULL,
    expires_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    revoked_at           TIMESTAMP(6) WITH TIME ZONE,
    replaced_by_token_id UUID,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

CREATE TABLE user_credentials
(
    id                  UUID                        NOT NULL,
    user_id             UUID                        NOT NULL,
    password_hash       VARCHAR(255)                NOT NULL,
    password_changed_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_user_credentials PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                UUID                        NOT NULL,
    email             VARCHAR(255)                NOT NULL,
    email_verified_at TIMESTAMP(6) WITH TIME ZONE,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    avatar_url        TEXT,
    default_currency  VARCHAR(3)                  NOT NULL,
    deleted_at        TIMESTAMP(6) WITH TIME ZONE,
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE email_verification_tokens
    ADD CONSTRAINT uk_email_verification_tokens_token_hash UNIQUE (token_hash);

ALTER TABLE oauth_accounts
    ADD CONSTRAINT uk_oauth_accounts_provider_user_id UNIQUE (provider, provider_user_id);

ALTER TABLE oauth_accounts
    ADD CONSTRAINT uk_oauth_accounts_user_provider UNIQUE (user_id, provider);

ALTER TABLE password_reset_tokens
    ADD CONSTRAINT uk_password_reset_tokens_token_hash UNIQUE (token_hash);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uk_refresh_tokens_token_hash UNIQUE (token_hash);

ALTER TABLE user_credentials
    ADD CONSTRAINT uk_user_credentials_user_id UNIQUE (user_id);