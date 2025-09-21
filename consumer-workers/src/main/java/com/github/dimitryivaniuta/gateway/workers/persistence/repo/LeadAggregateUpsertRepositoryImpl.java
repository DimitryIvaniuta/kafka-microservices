package com.github.dimitryivaniuta.gateway.workers.persistence.repo;

import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadAggregateEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class LeadAggregateUpsertRepositoryImpl implements LeadAggregateUpsertRepository {

    @PersistenceContext
    private final EntityManager em;

    @Transactional
    @Override
    public boolean insertIfAbsent(LeadAggregateEntity e) {
        final String sql = """
            INSERT INTO analytics.lead_aggregate
              (event_id, tenant_id, city, budget_usd, occurred_at, created_at)
            VALUES
              (:eventId, :tenantId, :city, :budgetUsd, :occurredAt, now())
            ON CONFLICT (event_id) DO NOTHING
            """;
        int updated = em.createNativeQuery(sql)
                .setParameter("eventId",   e.getEventId())
                .setParameter("tenantId",  e.getTenantId())
                .setParameter("city",      e.getCity())
                .setParameter("budgetUsd", e.getBudgetUsd())
                .setParameter("occurredAt",e.getOccurredAt())
                .executeUpdate();
        return updated == 1;
    }
}
