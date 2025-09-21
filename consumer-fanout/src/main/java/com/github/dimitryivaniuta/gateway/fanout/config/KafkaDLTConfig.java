package com.github.dimitryivaniuta.gateway.fanout.config;

import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Producer used by the error handler to publish to the DLT.
 * We build a minimal ProducerFactory here so the consumer app doesn't need the web/producer starter.
 */
@Configuration
public class KafkaDLTConfig {

    @Bean
    public ProducerFactory<String, Object> dltProducerFactory(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>(properties.buildProducerProperties());
        // Ensure serializers are set for DLT messages
        props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // We don't need transactions here; keep it simple and reliable.
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate(ProducerFactory<String, Object> dltProducerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(dltProducerFactory);
        // Optional: set a default topic to avoid null checks (we still route explicitly in the recoverer)
        template.setDefaultTopic(Topics.LEADS_DLT);
        return template;
    }
}
