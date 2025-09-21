package com.github.dimitryivaniuta.gateway.workers.persistence.repo;

import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeadAggregateReadRepository extends JpaRepository<LeadAggregateEntity, UUID> {
    boolean existsByEventId(UUID eventId);
}