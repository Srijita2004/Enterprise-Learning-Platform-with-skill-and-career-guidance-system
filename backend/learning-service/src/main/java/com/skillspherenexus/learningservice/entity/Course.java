package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID courseId;

    @Column(nullable = false, unique = true, length = 30)
    private String courseCode;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseType courseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseLevel courseLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CourseStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "pricing_type",
            nullable = false,
            length = 20,
            columnDefinition = "varchar(20) default 'FREE'"
    )
    private CoursePricingType pricingType;

    @Column(
            nullable = false,
            precision = 10,
            scale = 2,
            columnDefinition = "numeric(10,2) default 0.00"
    )
    private BigDecimal price;

    @Column(
            name = "currency_code",
            nullable = false,
            length = 3,
            columnDefinition = "varchar(3) default 'INR'"
    )
    private String currencyCode;

    @Column(nullable = false, length = 100)
    private String instructorName;

    @Column(nullable = false)
    private Integer durationHours;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer passingScore;

    @Column(nullable = false)
    private Boolean certificateEnabled;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(nullable = false)
    private Double averageRating;

    @Column(nullable = false)
    private Integer ratingCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime currentTime = LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (status == null) {
            status = CourseStatus.DRAFT;
        }

        if (pricingType == null) {
            pricingType = CoursePricingType.FREE;
        }

        if (price == null) {
            price = BigDecimal.ZERO;
        }

        if (currencyCode == null || currencyCode.isBlank()) {
            currencyCode = "INR";
        }

        if (maxCapacity == null) {
            maxCapacity = 100;
        }

        if (passingScore == null) {
            passingScore = 60;
        }

        if (certificateEnabled == null) {
            certificateEnabled = true;
        }

        if (averageRating == null) {
            averageRating = 0.0;
        }

        if (ratingCount == null) {
            ratingCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}