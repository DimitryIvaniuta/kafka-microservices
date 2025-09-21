package com.github.dimitryivaniuta.gateway.common.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable envelope for lead domain events.
 * This is the message type produced to Kafka and consumed by services.
 */
public record LeadEvent(
        UUID eventId,           // idempotency key (primary key at the sink)
        String tenantId,        // multi-tenant routing
        LeadEventType type,     // semantic type
        LeadPayload payload,    // lead data snapshot
        Instant occurredAt      // event time (producer clock, UTC)
) {
    public LeadEvent {
        Objects.requireNonNull(eventId,  "eventId is required");
        tenantId = trimToTenant(tenantId);
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(type,     "type is required");
        Objects.requireNonNull(payload,  "payload is required");
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
    }

    /** Kafka message key recommendation (keeps per-lead ordering): leadId. */
    public String key() {
        return payload.leadId();
    }

    // Handy classifiers
    public boolean isCreated()   { return type == LeadEventType.CREATED; }
    public boolean isUpdated()   { return type == LeadEventType.UPDATED; }
    public boolean isQualified() { return type == LeadEventType.QUALIFIED; }
    public boolean isRejected()  { return type == LeadEventType.REJECTED; }

    private static String trimToTenant(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Builder-style factory for convenience at the producer side. */
    public static LeadEvent now(UUID eventId, String tenantId, LeadEventType type, LeadPayload payload) {
        return new LeadEvent(eventId, tenantId, type, payload, Instant.now());
    }
}
