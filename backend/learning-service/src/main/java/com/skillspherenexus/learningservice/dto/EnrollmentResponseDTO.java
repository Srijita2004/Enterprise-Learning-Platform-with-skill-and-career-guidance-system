package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import com.skillspherenexus.learningservice.enums.EnrollmentSource;
import com.skillspherenexus.learningservice.enums.EnrollmentStatus;
import com.skillspherenexus.learningservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentResponseDTO {

    private UUID enrollmentId;

    private UUID learnerId;

    private String learnerName;

    private UUID courseId;

    private String courseCode;

    private String courseTitle;

    private CourseType courseType;

    private CourseStatus courseStatus;

    private CoursePricingType coursePricingType;

    private EnrollmentStatus status;

    private PaymentStatus paymentStatus;

    private String paymentReference;

    private LocalDateTime paymentSubmittedAt;

    private LocalDateTime paymentVerifiedAt;

    private UUID paymentVerifiedByUserId;

    private String paymentRejectionReason;

    private EnrollmentSource enrollmentSource;

    private UUID assignedByUserId;

    private BigDecimal priceAtEnrollment;

    private String currencyCode;

    private Boolean accessAllowed;

    private LocalDateTime enrolledAt;

    private LocalDateTime activatedAt;

    private LocalDateTime completedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime accessExpiresAt;

    private LocalDateTime lastAccessedAt;

    private String cancellationReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}