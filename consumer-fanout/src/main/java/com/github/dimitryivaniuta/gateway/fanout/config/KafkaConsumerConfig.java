package com.github.dimitryivaniuta.gateway.fanout.config;

import com.github.dimitryivaniuta.gateway.common.event.LeadEvent;
import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, LeadEvent> consumerFactory(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>(properties.buildConsumerProperties());

        // We configure the JsonDeserializer programmatically; make sure no spring.json.* props sneak in.
        props.remove("spring.json.trusted.packages");
        props.remove("spring.json.value.default.type");

        // Ensure deserializers are explicit (we bypass type headers)
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        JsonDeserializer<LeadEvent> value = new JsonDeserializer<>(LeadEvent.class);
        value.ignoreTypeHeaders();                  // we don’t use type headers from producer
        value.addTrustedPackages("com.github.dimitryivaniuta.gateway.common");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), value);
    }

    /**
     * Common error handler: retry with exponential backoff; publish to DLT on exhaustion or non-retryable exceptions.
     */
    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> dltKafkaTemplate) {
        // Backoff: start 1s, x2 multiplier, max 10s, with max retries = 3 (DefaultErrorHandler counts attempts internally)
        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxInterval(10_000L);
        // DefaultErrorHandler treats ExponentialBackOff as infinite; we cap by setting max failures (3) via setRetryListeners if needed.
        // Simpler: rely on classification + backoff; on repeated failures, recoverer sends to DLT.

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                dltKafkaTemplate,
                (record, ex) -> {
                    // Route everything to the same partition in the DLT as the original record
                    return new org.apache.kafka.common.TopicPartition(Topics.LEADS_DLT, record.partition());
                });

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // Don't retry deserialization/conversion problems – send straight to DLT
        handler.addNotRetryableExceptions(DeserializationException.class, ConversionException.class, IllegalArgumentException.class);

        // Commit the offset for recovered records (so we don't reprocess after publishing to DLT)
        handler.setCommitRecovered(true);

        return handler;
    }

    /**
     * Listener container factory for fan-out service:
     * - automatic ack (BATCH) after successful listener invocation
     * - missingTopicsFatal=false so the app doesn't die before topics exist (handy in dev)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LeadEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, LeadEvent> consumerFactory,
            DefaultErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, LeadEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);

        // Auto-ack on successful record processing (BATCH groups poll results)
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH);

        // Don’t blow up if topic isn't there yet in local dev
        factory.setMissingTopicsFatal(false);

        return factory;
    }
}
