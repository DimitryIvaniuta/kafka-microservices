package com.github.dimitryivaniuta.gateway.fanout.persistence.repo;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class LeadOffsetRepositoryImpl implements LeadOffsetRepository {

    private final EntityManager em;

    @Transactional
    @Override
    public void upsertOffset(String topic, int partitionId, long offset) {
        var sql = """
            INSERT INTO crm.lead_event_offset (topic, partition_id, last_offset, updated_at)
            VALUES (:topic, :pid, :off, now())
            ON CONFLICT (topic, partition_id)
            DO UPDATE SET last_offset = GREATEST(lead_event_offset.last_offset, EXCLUDED.last_offset),
                          updated_at  = now()
            """;
        em.createNativeQuery(sql)
                .setParameter("topic", topic)
                .setParameter("pid", partitionId)
                .setParameter("off", offset)
                .executeUpdate();
    }
}