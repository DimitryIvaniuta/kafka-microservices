package com.github.dimitryivaniuta.gateway.fanout.persistence.repo;


import com.github.dimitryivaniuta.gateway.fanout.persistence.entity.LeadEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class LeadUpsertRepositoryImpl implements LeadUpsertRepository {

    private final EntityManager em;

    @Transactional
    @Override
    public boolean insertIfAbsent(LeadEntity e) {
        var sql = """
            INSERT INTO crm.lead
              (event_id, tenant_id, lead_id, full_name, email, phone, city, source, budget_usd, occurred_at, created_at)
            VALUES
              (:eventId, :tenantId, :leadId, :fullName, :email, :phone, :city, :source, :budgetUsd, :occurredAt, now())
            ON CONFLICT (event_id) DO NOTHING
            """;
        var q = em.createNativeQuery(sql)
                .setParameter("eventId",   e.getEventId())
                .setParameter("tenantId",  e.getTenantId())
                .setParameter("leadId",    e.getLeadId())
                .setParameter("fullName",  e.getFullName())
                .setParameter("email",     e.getEmail())
                .setParameter("phone",     e.getPhone())
                .setParameter("city",      e.getCity())
                .setParameter("source",    e.getSource())
                .setParameter("budgetUsd", e.getBudgetUsd())
                .setParameter("occurredAt",e.getOccurredAt());
        int updated = q.executeUpdate();   // 1 = inserted, 0 = duplicate
        return updated == 1;
    }
}