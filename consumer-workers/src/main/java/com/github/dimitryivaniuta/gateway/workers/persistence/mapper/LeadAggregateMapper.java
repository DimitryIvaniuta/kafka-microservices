package com.github.dimitryivaniuta.gateway.workers.persistence.mapper;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadAggregateEntity;
import org.springframework.stereotype.Component;

/** Pure mapping from domain event -> analytics projection. */
@Component
public class LeadAggregateMapper {

    public LeadAggregateEntity toEntity(LeadEvent e) {
        var p = e.payload();
        return LeadAggregateEntity.builder()
                .eventId(e.eventId())
                .tenantId(e.tenantId())
                .city(p.city())
                .budgetUsd(p.budgetUsd())
                .occurredAt(e.occurredAt())
                .build();
    }
}