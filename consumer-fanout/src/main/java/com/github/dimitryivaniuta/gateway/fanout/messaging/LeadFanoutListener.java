package com.github.dimitryivaniuta.gateway.fanout.messaging;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import com.github.dimitryivaniuta.gateway.fanout.service.LeadIngestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Fan-out consumer: receives every record (own group) and persists idempotently.
 * Ack mode is configured in KafkaConsumerConfig (BATCH auto-ack).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeadFanoutListener {

    private final LeadIngestService ingest;

    @KafkaListener(
            topics = Topics.LEADS,
            groupId = "crm-fanout",
            concurrency = "${app.kafka.concurrency:3}"
    )
    public void onMessage(ConsumerRecord<String, LeadEvent> rec) {
        final LeadEvent event = rec.value();
        if (event == null) {
            // deserialization issues are handled by DefaultErrorHandler; just log a guard here
            log.warn("Received null LeadEvent at topic={} partition={} offset={}", rec.topic(), rec.partition(), rec.offset());
            return;
        }

        // Idempotent sink; single-statement upsert inside service
        boolean inserted = ingest.ingest(event);

        if (inserted && log.isInfoEnabled()) {
            log.info("Ingested lead eventId={} key={} partition={} offset={}",
                    event.eventId(), rec.key(), rec.partition(), rec.offset());
        } else if (log.isDebugEnabled()) {
            log.debug("Duplicate lead (already ingested) eventId={} partition={} offset={}",
                    event.eventId(), rec.partition(), rec.offset());
        }
    }
}
