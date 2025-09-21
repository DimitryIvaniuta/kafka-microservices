package com.github.dimitryivaniuta.gateway.producer.config;

import com.github.dimitryivaniuta.gateway.common.kafka.Topics;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * Creates topics on startup via AdminClient (enabled by spring.kafka.admin.*).
 * Defaults are safe for a single-broker dev stack; tune via env in higher envs.
 */
@Configuration
public class KafkaTopicsConfig {

    /**
     * Number of partitions for the main stream.
     * Increase to scale consumer parallelism (per consumer group) and throughput.
     */
    @Value("${app.kafka.topics.leads.partitions:3}")
    private int leadsPartitions;

    /**
     * Replication factor. In single-broker dev this must be 1; in prod use 3 (or more).
     */
    @Value("${app.kafka.topics.replication-factor:1}")
    private int replicationFactor;

    /**
     * Main domain topic for Real Estate leads.
     * Retention is long by default; tweak as needed. We keep delete policy (not compacted).
     */
    @Bean
    public NewTopic leadsTopic() {
        return TopicBuilder.name(Topics.LEADS)
                .partitions(leadsPartitions)
                .replicas(replicationFactor)
                // Example retention (14d) – align with broker defaults if you prefer
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(14L * 24 * 60 * 60 * 1000)) // 14 days
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    /**
     * Optional retry topic (if you implement delayed retries with a scheduler/bridge).
     * Created now so tooling/Kafka UI shows it; you can remove if not used.
     */
    @Bean
    public NewTopic leadsRetryTopic() {
        return TopicBuilder.name(Topics.LEADS_RETRY)
                .partitions(leadsPartitions)
                .replicas(replicationFactor)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7L * 24 * 60 * 60 * 1000)) // 7 days
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }

    /**
     * Dead-letter topic used by consumers' DefaultErrorHandler (DLT recoverer).
     * Keep long retention for forensic/audit; consider compaction if you plan dedupe downstream.
     */
    @Bean
    public NewTopic leadsDltTopic() {
        return TopicBuilder.name(Topics.LEADS_DLT)
                .partitions(leadsPartitions)     // match source partitions to preserve partition affinity
                .replicas(replicationFactor)
                .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(30L * 24 * 60 * 60 * 1000)) // 30 days
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
                .build();
    }
}
