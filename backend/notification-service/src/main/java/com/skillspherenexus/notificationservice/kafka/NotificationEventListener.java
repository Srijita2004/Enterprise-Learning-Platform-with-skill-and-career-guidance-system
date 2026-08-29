package com.skillspherenexus.notificationservice.kafka;

import com.skillspherenexus.notificationservice.enums.NotificationType;
import com.skillspherenexus.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Consumes the domain events published by M1 (skill-management-service),
 * M2 (learning-service), and M3 (certification-management-service) and
 * turns each one into a row in the Unified Notification Center.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${kafka.topic.employee-created}", groupId = "${spring.kafka.consumer.group-id}")
    public void onEmployeeCreated(Map<String, Object> event) {
        log.info("Received EmployeeCreatedEvent: {}", event);
        String employeeId = str(event.get("employeeId"));
        String employeeName = str(event.get("employeeName"));
        notificationService.createFromEvent(
                NotificationType.EMPLOYEE_CREATED,
                str(event.get("sourceService")),
                "New employee onboarded",
                employeeName + " has been added to the workforce",
                employeeId);
    }

    @KafkaListener(topics = "${kafka.topic.skill-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void onSkillUpdated(Map<String, Object> event) {
        log.info("Received SkillUpdatedEvent: {}", event);
        String employeeId = str(event.get("employeeId"));
        String skillId = str(event.get("skillId"));
        String changeType = str(event.get("changeType"));
        notificationService.createFromEvent(
                NotificationType.SKILL_UPDATED,
                str(event.get("sourceService")),
                "Skill profile " + (changeType == null ? "updated" : changeType.toLowerCase()),
                "Skill " + skillId + " was " + (changeType == null ? "updated" : changeType.toLowerCase())
                        + " for employee " + employeeId,
                employeeId);
    }

    @KafkaListener(topics = "${kafka.topic.course-completed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCourseCompleted(Map<String, Object> event) {
        log.info("Received CourseCompletedEvent: {}", event);
        String courseId = str(event.get("courseId"));
        String courseTitle = str(event.get("courseTitle"));
        String learnerId = str(event.get("learnerId"));
        notificationService.createFromEvent(
                NotificationType.COURSE_COMPLETED,
                str(event.get("sourceService")),
                "Course completed",
                "Course \"" + courseTitle + "\" was completed by learner " + learnerId,
                courseId);
    }

    @KafkaListener(topics = "${kafka.topic.certificate-issued}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCertificateIssued(Map<String, Object> event) {
        log.info("Received CertificateIssuedEvent: {}", event);
        String certificationId = str(event.get("certificationId"));
        String certificationName = str(event.get("certificationName"));
        String employeeName = str(event.get("employeeName"));
        notificationService.createFromEvent(
                NotificationType.CERTIFICATE_ISSUED,
                str(event.get("sourceService")),
                "New certification issued",
                certificationName + " issued to " + employeeName,
                certificationId);
    }

    @KafkaListener(topics = "${kafka.topic.certificate-renewed}", groupId = "${spring.kafka.consumer.group-id}")
    public void onCertificateRenewed(Map<String, Object> event) {
        log.info("Received CertificateRenewedEvent: {}", event);
        String certificationId = str(event.get("certificationId"));
        String certificationName = str(event.get("certificationName"));
        String newExpiryDate = str(event.get("newExpiryDate"));
        notificationService.createFromEvent(
                NotificationType.CERTIFICATE_RENEWED,
                str(event.get("sourceService")),
                "Certification renewed",
                certificationName + " renewed, new expiry " + newExpiryDate,
                certificationId);
    }

    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
