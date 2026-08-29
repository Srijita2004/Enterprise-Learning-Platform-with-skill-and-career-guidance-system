package com.skillspherenexus.skillmanagementservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.employee-created}")
    private String employeeCreatedTopic;

    @Value("${kafka.topic.skill-updated}")
    private String skillUpdatedTopic;

    @Bean
    public NewTopic employeeCreatedTopic() {
        return TopicBuilder.name(employeeCreatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic skillUpdatedTopic() {
        return TopicBuilder.name(skillUpdatedTopic).partitions(3).replicas(1).build();
    }
}
