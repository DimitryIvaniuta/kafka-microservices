package com.github.dimitryivaniuta.gateway.common.event;

import java.util.Objects;

/**
 * Immutable payload describing a Real-Estate lead at the time of the event.
 * All text fields are trimmed; required fields validated.
 */
public record LeadPayload(
        String leadId,
        String fullName,
        String email,
        String phone,
        String city,
        String source,
        Integer budgetUsd
) {
    public LeadPayload {
        // normalize/validate
        leadId   = trimToNull(leadId);
        fullName = trimToNull(fullName);
        email    = trimToNull(email);
        phone    = trimToNull(phone);
        city     = trimToNull(city);
        source   = trimToNull(source);

        Objects.requireNonNull(leadId,   "leadId is required");
        Objects.requireNonNull(fullName, "fullName is required");
        // email/phone can be null; budgetUsd optional
        if (budgetUsd != null && budgetUsd < 0) {
            throw new IllegalArgumentException("budgetUsd must be >= 0");
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
