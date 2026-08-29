package com.skillspherenexus.learningservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.course-completed}")
    private String courseCompletedTopic;

    @Bean
    public NewTopic courseCompletedTopic() {
        return TopicBuilder.name(courseCompletedTopic).partitions(3).replicas(1).build();
    }
}
