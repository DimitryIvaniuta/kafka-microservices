package com.github.dimitryivaniuta.gateway.producer.config;

import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Producer configuration:
 * - JSON value serializer (no type headers; consumers bind explicitly)
 * - Idempotent + transactional producer (exactly-once when combined with a transactional sink)
 * - Sensible batching/compression defaults for throughput
 */
@Configuration
public class KafkaProducerConfig {

    /**
     * Producer factory with enforced serializers and delivery guarantees.
     * Boot's KafkaProperties provide base props (bootstrap servers, etc.); we override critical settings here.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>(properties.buildProducerProperties());

        // Serializers
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // No type headers – consumers bind explicitly to LeadEvent
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        // Delivery guarantees + throughput
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 32_768);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30_000);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);

        DefaultKafkaProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(props);

        // Make the factory transaction-capable
        String txPrefix = properties.getProducer().getTransactionIdPrefix();
        if (txPrefix == null || txPrefix.isBlank()) {
            // fallback if not provided via application.yml/env
            txPrefix = "lead-tx-";
        }
        pf.setTransactionIdPrefix(txPrefix);

        return pf;
    }

    /**
     * Transaction manager for KafkaTemplate#executeInTransaction(...) and @Transactional("kafkaTransactionManager").
     * The transaction-id prefix should be configured in application.yml:
     *   spring.kafka.producer.transaction-id-prefix=lead-tx-
     */
    @Bean
    public KafkaTransactionManager<String, Object> kafkaTransactionManager(
            ProducerFactory<String, Object> producerFactory) {
        return new KafkaTransactionManager<>(producerFactory);
    }

    /**
     * Main KafkaTemplate used by the publisher service.
     * Transactions are active automatically when a transaction id prefix is configured.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        template.setDefaultTopic(Topics.LEADS);
        return template;
    }
}
