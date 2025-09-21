package com.github.dimitryivaniuta.gateway.workers.messaging;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import com.github.dimitryivaniuta.gateway.workers.service.LeadAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Worker listener (work-sharing): consumers in the same group share partitions.
 * Ack mode is MANUAL (configured in KafkaConsumerConfig) — we ack after the DB work succeeds.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadWorkerListener {

    private final LeadAnalyticsService service;

    @KafkaListener(
            topics = Topics.LEADS,
            groupId = "lead-workers",
            concurrency = "${app.kafka.concurrency:3}"
            // NOTE: All JSON deserializer settings are configured centrally in KafkaConsumerConfig.
    )
    public void onMessage(ConsumerRecord<String, LeadEvent> rec, Acknowledgment ack) {
        final LeadEvent event = rec.value();
        if (event == null) {
            // Deserialization errors are classified as non-retryable by DefaultErrorHandler and go to DLT.
            // This guard is only defensive.
            log.warn("Null LeadEvent at topic={} partition={} offset={}", rec.topic(), rec.partition(), rec.offset());
            return;
        }

        // Perform business projection + idempotent sink write.
        boolean inserted = service.process(rec.topic(), rec.partition(), rec.offset(), event);

        // Commit the Kafka offset after **successful** DB transaction
        ack.acknowledge();

        if (inserted && log.isInfoEnabled()) {
            log.info("Aggregated lead eventId={} key={} part={} off={}",
                    event.eventId(), rec.key(), rec.partition(), rec.offset());
        } else if (log.isDebugEnabled()) {
            log.debug("Duplicate (already aggregated) eventId={} part={} off={}",
                    event.eventId(), rec.partition(), rec.offset());
        }
    }
}
