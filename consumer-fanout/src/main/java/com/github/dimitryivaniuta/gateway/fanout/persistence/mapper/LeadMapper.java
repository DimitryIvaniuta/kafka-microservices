package com.github.dimitryivaniuta.gateway.fanout.persistence.mapper;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.common.event.LeadPayload;
import com.github.dimitryivaniuta.gateway.fanout.persistence.entity.LeadEntity;
import org.springframework.stereotype.Component;

/**
 * Pure mapping logic from domain event to persistence entity.
 * No side effects; easy to unit-test.
 */
@Component
public class LeadMapper {

    public LeadEntity toEntity(LeadEvent e) {
        LeadPayload p = e.payload();
        return LeadEntity.builder()
                .eventId(e.eventId())
                .tenantId(e.tenantId())
                .leadId(p.leadId())
                .fullName(p.fullName())
                .email(p.email())
                .phone(p.phone())
                .city(p.city())
                .source(p.source())
                .budgetUsd(p.budgetUsd())
                .occurredAt(e.occurredAt())
                .build();
    }
}
