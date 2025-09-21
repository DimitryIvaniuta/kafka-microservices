-- Schema for analytics workers (work-sharing)

SET search_path TO analytics;

-- Aggregation table with idempotency on event_id

CREATE TABLE IF NOT EXISTS lead_aggregate (
                                              id           BIGSERIAL PRIMARY KEY,
                                              event_id     UUID        NOT NULL,
                                              tenant_id    TEXT        NOT NULL,
                                              city         TEXT,
                                              budget_usd   INTEGER,
                                              occurred_at  TIMESTAMPTZ NOT NULL,
                                              created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (event_id)
    );

CREATE INDEX IF NOT EXISTS idx_leadagg_tenant ON lead_aggregate (tenant_id);
CREATE INDEX IF NOT EXISTS idx_leadagg_city   ON lead_aggregate (city);
CREATE INDEX IF NOT EXISTS idx_leadagg_time   ON lead_aggregate (occurred_at);

-- Optional offset tracking

CREATE TABLE IF NOT EXISTS lead_event_offset (
                                                 id             BIGSERIAL PRIMARY KEY,
                                                 topic          TEXT        NOT NULL,
                                                 partition_id   INT         NOT NULL,
                                                 last_offset    BIGINT      NOT NULL,
                                                 updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (topic, partition_id)
);