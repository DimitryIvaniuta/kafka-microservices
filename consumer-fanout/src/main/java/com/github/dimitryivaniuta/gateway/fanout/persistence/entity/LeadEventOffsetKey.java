package com.github.dimitryivaniuta.gateway.fanout.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LeadEventOffsetKey {

    /** Kafka topic name (e.g., leads.events). */
    @Column(name = "topic", nullable = false)       private String topic;

    /** Partition id in the topic (renamed from 'partition' to avoid SQL keyword). */
    @Column(name = "partition_id", nullable = false) private Integer partitionId;
}