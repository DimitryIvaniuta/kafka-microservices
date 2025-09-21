package com.github.dimitryivaniuta.gateway.producer.service;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.common.event.LeadEventType;
import com.github.dimitryivaniuta.gateway.common.event.LeadPayload;
import com.github.dimitryivaniuta.gateway.common.kafka.Headers;
import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import com.github.dimitryivaniuta.gateway.producer.api.dto.LeadCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Publishes domain events for Real-Estate leads.
 * - Idempotent semantics are enforced downstream (eventId is the sink PK).
 * - Uses Kafka transactions via "kafkaTransactionManager" (configured in KafkaProducerConfig).
 * - No type headers in JSON; consumers bind to LeadEvent.class programmatically.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeadPublisher {

    private static final String DEFAULT_TENANT = "default";

    private final KafkaTemplate<String, Object> kafka;

    /**
     * Creates and publishes a LeadEvent(CREATED) from a REST DTO.
     * @return generated eventId (idempotency key).
     */
    @Transactional("kafkaTransactionManager")
    public UUID publishCreate(LeadCreateRequest req, String tenantHeader) {
        Objects.requireNonNull(req, "LeadCreateRequest must not be null");

        final String tenantId = normalizeTenant(tenantHeader);
        final UUID eventId = UUID.randomUUID();
        final String leadId = req.leadId() != null && !req.leadId().isBlank()
                ? req.leadId().trim()
                : UUID.randomUUID().toString();

        final LeadPayload payload = new LeadPayload(
                leadId,
                req.fullName(),
                req.email(),
                req.phone(),
                req.city(),
                req.source(),
                req.budgetUsd()
        );

        final LeadEvent event = new LeadEvent(
                eventId,
                tenantId,
                LeadEventType.CREATED,
                payload,
                Instant.now()
        );

        // Kafka key: keep per-lead ordering across the stream
        final String key = event.key();

        // Propagate correlation/trace id from MDC if present
        final String traceId = MDC.get("traceId");

        Message<LeadEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, Topics.LEADS)
                .setHeader(KafkaHeaders.KEY, key)
                .setHeader(Headers.EVENT_ID, eventId.toString())
                .setHeader(Headers.TENANT_ID, tenantId)
                .setHeader(Headers.TYPE, event.type().name())
                .setHeader(Headers.TRACE_ID, traceId)
                .build();

        // Send within the Kafka transaction (active due to @Transactional with Kafka TM)
        kafka.send(message).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish lead eventId={} key={}", eventId, key, ex);
            } else if (result != null && result.getRecordMetadata() != null) {
                var md = result.getRecordMetadata();
                log.info("Published lead eventId={} key={} topic={} partition={} offset={}",
                        eventId, key, md.topic(), md.partition(), md.offset());
            } else {
                log.info("Published lead eventId={} key={} (no metadata available)", eventId, key);
            }
        });

        return eventId;
    }

    private static String normalizeTenant(String tenantHeader) {
        if (tenantHeader == null) return DEFAULT_TENANT;
        String t = tenantHeader.trim();
        return t.isEmpty() ? DEFAULT_TENANT : t;
    }
}
