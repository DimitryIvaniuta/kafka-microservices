package com.github.dimitryivaniuta.gateway.workers.service;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadAggregateEntity;
import com.github.dimitryivaniuta.gateway.workers.persistence.mapper.LeadAggregateMapper;
import com.github.dimitryivaniuta.gateway.workers.persistence.repo.LeadAggregateUpsertRepository;
import com.github.dimitryivaniuta.gateway.workers.persistence.repo.LeadEventOffsetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates lead events into the analytics projection.
 * Uses single-statement UPSERT for idempotency; updates a monotonic offset row per (topic, partition).
 */
@Service
@RequiredArgsConstructor
public class LeadAnalyticsService {

    private final LeadAggregateMapper mapper;
    private final LeadAggregateUpsertRepository upsertRepo;
    private final LeadEventOffsetRepository offsetRepo;

    /**
     * @return true if a new aggregate row was inserted; false if the event was already processed.
     *
     * The transaction boundary ensures DB work is atomic from the application's perspective:
     * - If the upsert throws, the listener will not ack and DefaultErrorHandler will handle retry/DLT.
     * - Offsets table is updated with GREATEST(...) semantics to avoid rewinds.
     */
    @Transactional
    public boolean process(String topic, int partition, long offset, LeadEvent event) {
        // 1) Map domain event -> analytics projection
        LeadAggregateEntity agg = mapper.toEntity(event);

        // 2) Idempotent write (INSERT ... ON CONFLICT DO NOTHING)
        boolean inserted = upsertRepo.insertIfAbsent(agg);

        // 3) Optional: track last committed Kafka offset per (topic, partition)
        //    Uses INSERT ... ON CONFLICT DO UPDATE with GREATEST(last_offset, excluded.last_offset)
        offsetRepo.upsertOffset(topic, partition, offset);

        return inserted;
    }
}
