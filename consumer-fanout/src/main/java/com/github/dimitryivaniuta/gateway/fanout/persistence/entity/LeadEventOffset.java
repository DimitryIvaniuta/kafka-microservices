package com.github.dimitryivaniuta.gateway.fanout.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Maps to table crm.lead_event_offset
 * Flyway DDL:
 *  - UNIQUE (topic, partition_id)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "lead_event_offset"
)
public class LeadEventOffset {

    @EmbeddedId
    private LeadEventOffsetKey id;

    /** Last successfully processed/committed offset for this (topic,partition). */
    @Column(name = "last_offset", nullable = false)
    private Long lastOffset;

    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    private void onUpdate() {
        updatedAt = Instant.now();
    }

//    public static LeadEventOffset initial(String topic, int partitionId) {
//        return LeadEventOffset.builder()
//                .topic(topic)
//                .partitionId(partitionId)
//                .lastOffset(-1L)   // before first record
//                .build();
//    }

    public LeadEventOffset advanceTo(long offset) {
        this.lastOffset = offset;
        // updatedAt will be set by @PreUpdate when persisted
        return this;
    }
}
