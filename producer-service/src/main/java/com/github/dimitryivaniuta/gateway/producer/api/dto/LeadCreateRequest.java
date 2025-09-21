package com.github.dimitryivaniuta.gateway.producer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming REST payload to create a lead event.
 * - Only fullName is required; leadId is optional (server will generate if missing/blank).
 * - Email/phone/city/source are optional; budgetUsd must be >= 0 when present.
 */
public record LeadCreateRequest(

        /** Optional client-provided id; if blank/missing the server generates one. */
        @Size(max = 120)
        String leadId,

        /** Full name is required for a meaningful lead. */
        @NotBlank
        @Size(max = 200)
        String fullName,

        /** Optional contact email. */
        @Email
        @Size(max = 320)
        String email,

        /** Optional phone (basic max length guard; formatting validated upstream if needed). */
        @Size(max = 40)
        String phone,

        /** Optional city field (for geo targeting/analytics). */
        @Size(max = 120)
        String city,

        /** Optional acquisition source (e.g., "web", "fb-ads", "referral"). */
        @Size(max = 120)
        String source,

        /** Optional budget; must be non-negative if present. */
        @Min(0)
        Integer budgetUsd
) { }
