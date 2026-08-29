package com.skillspherenexus.certificationmanagementservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.certificate-issued}")
    private String certificateIssuedTopic;

    @Value("${kafka.topic.certificate-renewed}")
    private String certificateRenewedTopic;

    @Bean
    public NewTopic certificateIssuedTopic() {
        return TopicBuilder.name(certificateIssuedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic certificateRenewedTopic() {
        return TopicBuilder.name(certificateRenewedTopic).partitions(3).replicas(1).build();
    }
}
