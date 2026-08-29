package com.skillspherenexus.notificationservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Configuration
public class NotificationTopicConfig {

    @Value("${kafka.topic.notification-created}")
    private String notificationCreatedTopic;

    @Bean
    public NewTopic notificationCreatedTopic() {
        return TopicBuilder.name(notificationCreatedTopic).partitions(3).replicas(1).build();
    }
}

