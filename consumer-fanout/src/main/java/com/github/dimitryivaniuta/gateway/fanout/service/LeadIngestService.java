package com.github.dimitryivaniuta.gateway.fanout.service;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.fanout.persistence.entity.LeadEntity;
import com.github.dimitryivaniuta.gateway.fanout.persistence.mapper.LeadMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Performs idempotent persistence of LeadEvent using a single UPSERT statement.
 * Returns true if a new row was inserted, false if it already existed.
 */
@Service
@RequiredArgsConstructor
public class LeadIngestService {

    private final LeadMapper mapper;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public boolean ingest(LeadEvent e) {
        final LeadEntity le = mapper.toEntity(e);

        // Single-statement UPSERT (race-free, no exception-driven flow)
        final String sql = """
            INSERT INTO crm.lead
              (event_id, tenant_id, lead_id, full_name, email, phone, city, source, budget_usd, occurred_at, created_at)
            VALUES
              (:eventId, :tenantId, :leadId, :fullName, :email, :phone, :city, :source, :budgetUsd, :occurredAt, now())
            ON CONFLICT (event_id) DO NOTHING
            """;

        int updated = em.createNativeQuery(sql)
                .setParameter("eventId",   le.getEventId())
                .setParameter("tenantId",  le.getTenantId())
                .setParameter("leadId",    le.getLeadId())
                .setParameter("fullName",  le.getFullName())
                .setParameter("email",     le.getEmail())
                .setParameter("phone",     le.getPhone())
                .setParameter("city",      le.getCity())
                .setParameter("source",    le.getSource())
                .setParameter("budgetUsd", le.getBudgetUsd())
                .setParameter("occurredAt",le.getOccurredAt())
                .executeUpdate();

        return updated == 1; // 1 = inserted, 0 = duplicate
    }
}
