CREATE TABLE platform.email_verification_token
(
  id              UUID PRIMARY KEY,
  user_id         UUID NOT NULL
    REFERENCES platform.user_account(id)
      ON DELETE CASCADE,
  token_hash      VARCHAR(64) NOT NULL UNIQUE,
  expires_at      TIMESTAMPTZ NOT NULL,
  consumed_at     TIMESTAMPTZ,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT email_verification_token_hash_length
    CHECK (LENGTH(token_hash) = 64)
);

CREATE INDEX ix_email_verification_user_created
  ON platform.email_verification_token (user_id, created_at DESC);

CREATE INDEX ix_email_verification_expiry
  ON platform.email_verification_token (expires_at);

COMMENT ON TABLE platform.email_verification_token
    IS 'Single-use, time-limited owner email verification tokens.';

COMMENT ON COLUMN platform.email_verification_token.token_hash
    IS 'SHA-256 hash of the verification token. Raw tokens are never stored.';


CREATE TABLE platform.email_outbox
(
  id                  UUID PRIMARY KEY,
  recipient_email     VARCHAR(320) NOT NULL,

  payload_nonce       BYTEA,
  payload_ciphertext  BYTEA,

  status              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  attempts            INTEGER NOT NULL DEFAULT 0,
  available_at        TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  locked_at           TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  sent_at             TIMESTAMPTZ,
  last_error_code     VARCHAR(80),

  CONSTRAINT email_outbox_status_check
    CHECK (
      status IN (
                 'PENDING',
                 'PROCESSING',
                 'RETRY',
                 'SENT',
                 'DEAD'
        )
      ),

  CONSTRAINT email_outbox_attempts_check
    CHECK (attempts >= 0),

  CONSTRAINT email_outbox_payload_check
    CHECK (
      (
        status = 'SENT'
          AND payload_nonce IS NULL
          AND payload_ciphertext IS NULL
        )
        OR
      (
        status <> 'SENT'
          AND payload_nonce IS NOT NULL
          AND payload_ciphertext IS NOT NULL
          AND OCTET_LENGTH(payload_nonce) = 12
          AND OCTET_LENGTH(payload_ciphertext) > 16
        )
      )
);

CREATE INDEX ix_email_outbox_dispatch
  ON platform.email_outbox (status, available_at, created_at);

CREATE INDEX ix_email_outbox_processing
  ON platform.email_outbox (locked_at)
  WHERE status = 'PROCESSING';

COMMENT ON TABLE platform.email_outbox
    IS 'Durable, encrypted outbound email requests with retry state.';

COMMENT ON COLUMN platform.email_outbox.payload_ciphertext
    IS 'AES-256-GCM encrypted email subject and body.';

COMMENT ON COLUMN platform.email_outbox.payload_nonce
    IS 'Unique 96-bit nonce for AES-GCM encryption.';


CREATE TABLE platform.public_request_rate_limit
(
  action                  VARCHAR(40) NOT NULL,
  fingerprint_hash        CHAR(64) NOT NULL,
  window_started_at       TIMESTAMPTZ NOT NULL,
  request_count           INTEGER NOT NULL DEFAULT 0,
  updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT public_request_rate_limit_pk
    PRIMARY KEY (
                 action,
                 fingerprint_hash,
                 window_started_at
      ),

  CONSTRAINT public_request_rate_limit_count_check
    CHECK (request_count >= 0)
);

CREATE INDEX ix_public_request_rate_limit_updated
  ON platform.public_request_rate_limit (updated_at);

COMMENT ON TABLE platform.public_request_rate_limit
    IS 'Short-lived public endpoint rate-limit counters. Client addresses are stored only as hashes.';
