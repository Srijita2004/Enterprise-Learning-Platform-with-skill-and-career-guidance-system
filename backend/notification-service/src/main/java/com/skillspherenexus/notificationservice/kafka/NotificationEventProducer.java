package com.skillspherenexus.notificationservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component; /**
 * Publishes a lightweight "notification-created" event whenever a new
 * Notification row is persisted, so other services (e.g. audit/reporting)
 * can react without querying this service's database directly.
 */
@Component
public class NotificationEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String notificationCreatedTopic;

    NotificationEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                               @Value("${kafka.topic.notification-created}") String notificationCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.notificationCreatedTopic = notificationCreatedTopic;
    }

    public void publish(Object notificationId, Object payload) {
        kafkaTemplate.send(notificationCreatedTopic, String.valueOf(notificationId), payload);
    }
}
