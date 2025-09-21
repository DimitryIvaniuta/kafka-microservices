package com.github.dimitryivaniuta.gateway.fanout.persistence.repo;

import com.github.dimitryivaniuta.gateway.fanout.persistence.entity.LeadEntity;

public interface LeadUpsertRepository {
    /**
     * @return true if inserted (new), false if duplicate (already existed)
     */
    boolean insertIfAbsent(LeadEntity e);
}