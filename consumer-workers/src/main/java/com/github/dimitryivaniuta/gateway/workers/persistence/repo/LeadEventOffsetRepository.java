package com.github.dimitryivaniuta.gateway.workers.persistence.repo;

public interface LeadEventOffsetRepository {
    /** Upsert and monotonically advance the stored offset. */
    void upsertOffset(String topic, int partitionId, long offset);
}