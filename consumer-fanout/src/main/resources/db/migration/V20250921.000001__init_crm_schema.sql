-- Schema for CRM fan-out consumer (receives every event)

-- Ensure we use it for object creation
SET search_path TO crm;

-- Main entity table with idempotency on event_id
CREATE TABLE IF NOT EXISTS lead (
    event_id     UUID        NOT NULL PRIMARY KEY,
    tenant_id    TEXT        NOT NULL,
    lead_id      TEXT        NOT NULL,
    full_name    TEXT        NOT NULL,
    email        TEXT,
    phone        TEXT,
    city         TEXT,
    source       TEXT,
    budget_usd   INTEGER,
    occurred_at  TIMESTAMPTZ NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (event_id)
    );

-- Helpful indexes for queries
CREATE INDEX IF NOT EXISTS idx_lead_tenant      ON lead (tenant_id);
CREATE INDEX IF NOT EXISTS idx_lead_lead_id     ON lead (lead_id);
CREATE INDEX IF NOT EXISTS idx_lead_occurred_at ON lead (occurred_at);

-- Optional: track offsets for audits/ops
CREATE TABLE IF NOT EXISTS lead_event_offset (
    topic          TEXT        NOT NULL,
    partition_id   INT         NOT NULL,  -- renamed from 'partition'
    last_offset    BIGINT      NOT NULL,  -- renamed from 'offset'
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (topic, partition_id)
);
