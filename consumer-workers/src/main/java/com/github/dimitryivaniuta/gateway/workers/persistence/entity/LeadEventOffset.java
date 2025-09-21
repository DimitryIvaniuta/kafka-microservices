package com.github.dimitryivaniuta.gateway.workers.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * analytics.lead_event_offset
 * Composite PK (topic, partition_id). Monotonically advances last_offset.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "lead_event_offset")
public class LeadEventOffset {

    @EmbeddedId
    private LeadEventOffsetKey id;

    @Column(name = "last_offset", nullable = false)
    private Long lastOffset;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public static LeadEventOffset initial(String topic, int partition) {
        return LeadEventOffset.builder()
                .id(new LeadEventOffsetKey(topic, partition))
                .lastOffset(-1L)
                .build();
    }
}