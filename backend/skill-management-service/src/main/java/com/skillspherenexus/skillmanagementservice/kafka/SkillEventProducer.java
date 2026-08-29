package com.skillspherenexus.skillmanagementservice.kafka;

import com.skillspherenexus.skillmanagementservice.event.EmployeeCreatedEvent;
import com.skillspherenexus.skillmanagementservice.event.SkillUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes M1 domain events to Kafka. Failures are logged rather than
 * thrown so that a Kafka outage never breaks the core CRUD flow.
 */
@Component
public class SkillEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(SkillEventProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.employee-created}")
    private String employeeCreatedTopic;

    @Value("${kafka.topic.skill-updated}")
    private String skillUpdatedTopic;

    public void publishEmployeeCreated(EmployeeCreatedEvent event) {
        try {
            kafkaTemplate.send(employeeCreatedTopic, String.valueOf(event.getEmployeeId()), event);
            logger.info("Published EmployeeCreatedEvent for employeeId={}", event.getEmployeeId());
        } catch (Exception ex) {
            logger.error("Failed to publish EmployeeCreatedEvent for employeeId={}", event.getEmployeeId(), ex);
        }
    }

    public void publishSkillUpdated(SkillUpdatedEvent event) {
        try {
            kafkaTemplate.send(skillUpdatedTopic, String.valueOf(event.getEmployeeId()), event);
            logger.info("Published SkillUpdatedEvent for employeeId={}, skillId={}", event.getEmployeeId(), event.getSkillId());
        } catch (Exception ex) {
            logger.error("Failed to publish SkillUpdatedEvent for employeeId={}, skillId={}", event.getEmployeeId(), event.getSkillId(), ex);
        }
    }
}
