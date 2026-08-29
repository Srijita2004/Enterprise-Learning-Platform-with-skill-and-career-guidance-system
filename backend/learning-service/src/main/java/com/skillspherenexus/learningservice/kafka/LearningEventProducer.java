package com.skillspherenexus.learningservice.kafka;

import com.skillspherenexus.learningservice.event.CourseCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes M2 domain events to Kafka. Failures are logged rather than
 * thrown so that a Kafka outage never breaks the core completion flow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LearningEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.course-completed}")
    private String courseCompletedTopic;

    public void publishCourseCompleted(CourseCompletedEvent event) {
        try {
            kafkaTemplate.send(courseCompletedTopic, String.valueOf(event.getLearnerId()), event);
            log.info("Published CourseCompletedEvent for learnerId={}, courseId={}",
                    event.getLearnerId(), event.getCourseId());
        } catch (Exception ex) {
            log.error("Failed to publish CourseCompletedEvent for learnerId={}, courseId={}",
                    event.getLearnerId(), event.getCourseId(), ex);
        }
    }
}
