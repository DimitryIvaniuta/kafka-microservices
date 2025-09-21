package com.github.dimitryivaniuta.gateway.fanout.persistence.repo;

public interface LeadOffsetRepository {
    void upsertOffset(String topic, int partitionId, long offset);
}