package com.github.dimitryivaniuta.gateway.workers.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * analytics.lead_aggregate
 * PK on event_id (idempotency); secondary indexes are created via Flyway.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "lead_aggregate")
public class LeadAggregateEntity {

    /** Natural idempotency key – map to PK for best write perf. */
    @Id
    @Column(name = "event_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "city")
    private String city;

    @Column(name = "budget_usd")
    private Integer budgetUsd;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
