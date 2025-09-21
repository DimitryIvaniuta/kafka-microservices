package com.github.dimitryivaniuta.gateway.common.kafka;

/**
 * Centralized topic names used across services.
 * Keep these constants the single source of truth to avoid typos.
 */
public final class Topics {

    private Topics() { /* no instances */ }

    /** Primary stream of Real-Estate lead domain events. */
    public static final String LEADS = "leads.events";

    /** Optional retry topic (if you implement delayed retries). */
    public static final String LEADS_RETRY = "leads.events.retry";

    /** Dead-letter topic for unrecoverable or exhausted failures. */
    public static final String LEADS_DLT = "leads.events.DLT";
}
