-- Establish the initial schema boundary for SoloCrew platform-owned data.
--
-- Business capabilities will introduce their own tables through
-- subsequent versioned migrations. This migration intentionally
-- does not create business tables.

CREATE SCHEMA IF NOT EXISTS platform;

COMMENT ON SCHEMA platform
    IS 'SoloCrew platform-owned database objects.';
