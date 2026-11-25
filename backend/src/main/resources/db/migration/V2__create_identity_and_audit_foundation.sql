CREATE TABLE platform.user_account
(
  id                  UUID PRIMARY KEY,
  email               VARCHAR(320) NOT NULL,
  display_name        VARCHAR(120) NOT NULL,
  password_hash       VARCHAR(255) NOT NULL,
  status              VARCHAR(40) NOT NULL
                                           DEFAULT 'PENDING_EMAIL_VERIFICATION',
  email_verified_at   TIMESTAMPTZ,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT user_account_status_check
    CHECK (
      status IN (
                 'PENDING_EMAIL_VERIFICATION',
                 'ACTIVE',
                 'DISABLED'
        )
      ),

  CONSTRAINT user_account_active_requires_verified_email
    CHECK (
      status <> 'ACTIVE'
        OR email_verified_at IS NOT NULL
      )
);

CREATE UNIQUE INDEX uq_user_account_normalized_email
  ON platform.user_account (LOWER(BTRIM(email)));

COMMENT ON TABLE platform.user_account
    IS 'SoloCrew user identities and account lifecycle state.';

COMMENT ON COLUMN platform.user_account.password_hash
    IS 'Adaptive one-way password hash. Never store plaintext passwords.';


CREATE TABLE platform.business_workspace
(
  id                  UUID PRIMARY KEY,
  name                VARCHAR(160) NOT NULL,
  slug                VARCHAR(80) NOT NULL UNIQUE,
  status              VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_by_user_id  UUID NOT NULL
    REFERENCES platform.user_account(id),
  created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT business_workspace_status_check
    CHECK (
      status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')
      ),

  CONSTRAINT business_workspace_slug_lowercase
    CHECK (slug = LOWER(slug))
);

COMMENT ON TABLE platform.business_workspace
    IS 'Business workspaces operated through SoloCrew.';


CREATE TABLE platform.workspace_membership
(
  id              UUID PRIMARY KEY,
  workspace_id    UUID NOT NULL
    REFERENCES platform.business_workspace(id),
  user_id         UUID NOT NULL
    REFERENCES platform.user_account(id),
  role            VARCHAR(30) NOT NULL,
  status          VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT workspace_membership_role_check
    CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),

  CONSTRAINT workspace_membership_status_check
    CHECK (status IN ('ACTIVE', 'INVITED', 'SUSPENDED')),

  CONSTRAINT workspace_membership_unique_user
    UNIQUE (workspace_id, user_id)
);

CREATE INDEX ix_workspace_membership_user
  ON platform.workspace_membership (user_id, status);

CREATE INDEX ix_workspace_membership_workspace
  ON platform.workspace_membership (workspace_id, status);

COMMENT ON TABLE platform.workspace_membership
    IS 'User membership and role within a business workspace.';


CREATE TABLE platform.audit_event
(
  event_id        UUID PRIMARY KEY,
  occurred_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

  -- Historical identifiers intentionally have no foreign keys.
  -- Audit records must survive changes to the referenced resources.
  workspace_id    UUID,

  actor_type      VARCHAR(40) NOT NULL,
  actor_id        UUID,
  event_type      VARCHAR(120) NOT NULL,
  action          VARCHAR(80) NOT NULL,
  resource_type   VARCHAR(100),
  resource_id     VARCHAR(200),
  outcome         VARCHAR(30) NOT NULL,
  correlation_id  VARCHAR(128),
  metadata        JSONB NOT NULL DEFAULT '{}'::JSONB,

  CONSTRAINT audit_event_actor_type_check
    CHECK (
      actor_type IN (
                     'USER',
                     'DIGITAL_EMPLOYEE',
                     'SYSTEM',
                     'PLATFORM_OPERATOR'
        )
      ),

  CONSTRAINT audit_event_outcome_check
    CHECK (
      outcome IN ('SUCCESS', 'FAILURE', 'DENIED')
      ),

  CONSTRAINT audit_event_metadata_object_check
    CHECK (JSONB_TYPEOF(metadata) = 'object')
);

CREATE INDEX ix_audit_event_workspace_time
  ON platform.audit_event (workspace_id, occurred_at DESC);

CREATE INDEX ix_audit_event_actor_time
  ON platform.audit_event (actor_type, actor_id, occurred_at DESC);

CREATE INDEX ix_audit_event_correlation
  ON platform.audit_event (correlation_id);

CREATE INDEX ix_audit_event_type_time
  ON platform.audit_event (event_type, occurred_at DESC);

COMMENT ON TABLE platform.audit_event
    IS 'Append-only record of security, business and system events.';


CREATE OR REPLACE FUNCTION platform.reject_audit_event_mutation()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION
        'Audit events are append-only. Operation % is not allowed.',
        TG_OP
        USING ERRCODE = '55000';
END;
$$;

CREATE TRIGGER trg_audit_event_no_update_delete
  BEFORE UPDATE OR DELETE
ON platform.audit_event
    FOR EACH ROW
    EXECUTE FUNCTION platform.reject_audit_event_mutation();

CREATE TRIGGER trg_audit_event_no_truncate
  BEFORE TRUNCATE
  ON platform.audit_event
    FOR EACH STATEMENT
    EXECUTE FUNCTION platform.reject_audit_event_mutation();

REVOKE UPDATE, DELETE, TRUNCATE
  ON platform.audit_event
  FROM PUBLIC;
