package com.github.dimitryivaniuta.gateway.common.kafka;

/**
 * Header keys used on Kafka records.
 * Use these constants consistently for producing and consuming.
 */
public final class Headers {

    private Headers() { /* no instances */ }

    /** Unique event identifier (UUID string) for idempotency and tracing. */
    public static final String EVENT_ID = "x-event-id";

    /** Tenant (organization/account) identifier for multi-tenancy. */
    public static final String TENANT_ID = "x-tenant-id";

    /** Trace or correlation id (propagated from HTTP/request context). */
    public static final String TRACE_ID = "x-trace-id";

    /** Logical event type (e.g., CREATED/UPDATED/QUALIFIED/REJECTED). */
    public static final String TYPE = "x-event-type";
}
