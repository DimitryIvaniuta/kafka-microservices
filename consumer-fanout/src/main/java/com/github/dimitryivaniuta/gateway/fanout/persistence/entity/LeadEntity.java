package com.github.dimitryivaniuta.gateway.fanout.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Maps to table crm.lead
 * Flyway DDL:
 *  - UNIQUE (event_id)
 *  - indexes on (tenant_id), (lead_id), (occurred_at)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "lead",
        indexes = {
                @Index(name = "idx_lead_tenant", columnList = "tenant_id"),
                @Index(name = "idx_lead_lead_id", columnList = "lead_id"),
                @Index(name = "idx_lead_occurred_at", columnList = "occurred_at")
        }
)
public class LeadEntity {


    @Id
    @Column(name = "event_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID eventId;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "lead_id", nullable = false)
    private String leadId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "city")
    private String city;

    @Column(name = "source")
    private String source;

    @Column(name = "budget_usd")
    private Integer budgetUsd;

    @Column(name = "occurred_at", nullable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
    private Instant createdAt;

    @PrePersist
    private void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    /**
     * Natural equality by unique business key (eventId).
     * NOTE: eventId is always set from the Kafka event before persist.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LeadEntity that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    /* ---------- Convenience factory ---------- */

    public static LeadEntity from(
            java.util.UUID eventId,
            String tenantId,
            String leadId,
            String fullName,
            String email,
            String phone,
            String city,
            String source,
            Integer budgetUsd,
            Instant occurredAt
    ) {
        return LeadEntity.builder()
                .eventId(eventId)
                .tenantId(tenantId)
                .leadId(leadId)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .city(city)
                .source(source)
                .budgetUsd(budgetUsd)
                .occurredAt(occurredAt)
                .build();
    }

    /**
     * Helper to map directly from your common LeadEvent record.
     * (Adjust FQCN if your package differs.)
     */
    public static LeadEntity from(com.github.dimitryivaniuta.gateway.common.event.LeadEvent e) {
        var p = e.payload();
        return from(
                e.eventId(),
                e.tenantId(),
                p.leadId(),
                p.fullName(),
                p.email(),
                p.phone(),
                p.city(),
                p.source(),
                p.budgetUsd(),
                e.occurredAt()
        );
    }
}
