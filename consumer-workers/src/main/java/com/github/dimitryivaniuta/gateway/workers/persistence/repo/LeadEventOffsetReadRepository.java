package com.github.dimitryivaniuta.gateway.workers.persistence.repo;

import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadEventOffset;
import com.github.dimitryivaniuta.gateway.workers.persistence.entity.LeadEventOffsetKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadEventOffsetReadRepository extends JpaRepository<LeadEventOffset, LeadEventOffsetKey> {
}