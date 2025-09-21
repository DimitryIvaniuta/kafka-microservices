package com.github.dimitryivaniuta.gateway.common.event;

/**
 * Domain-level classification of lead events.
 * Extend as your workflow evolves.
 */
public enum LeadEventType {
    CREATED,
    UPDATED,
    QUALIFIED,
    REJECTED
}
