package com.skillspherenexus.learningservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published to the "course-completed" Kafka topic whenever a learner
 * finishes a course in the Learning Service (M2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseCompletedEvent implements Serializable {

    private UUID completionId;
    private UUID enrollmentId;
    private UUID learnerId;
    private UUID courseId;
    private String courseTitle;
    private Boolean certificateEligible;
    @Builder.Default
    private String sourceService = "learning-service";
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();
}
