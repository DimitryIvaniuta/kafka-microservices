package com.github.dimitryivaniuta.gateway.workers.persistence.repo;

import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadAggregateEntity;

public interface LeadAggregateUpsertRepository {
    /**
     * @return true if inserted (new), false if duplicate (event already seen)
     */
    boolean insertIfAbsent(LeadAggregateEntity entity);
}