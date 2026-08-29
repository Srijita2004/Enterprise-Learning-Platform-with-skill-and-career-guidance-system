package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseStatus;
import com.skillspherenexus.learningservice.enums.CourseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponseDTO {

    private UUID courseId;

    private String courseCode;

    private String title;

    private String description;

    private String category;

    private CourseType courseType;

    private CourseLevel courseLevel;

    private CourseStatus status;

    private CoursePricingType pricingType;

    private BigDecimal price;

    private String currencyCode;

    private String instructorName;

    private Integer durationHours;

    private Integer maxCapacity;

    private Integer passingScore;

    private Boolean certificateEnabled;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double averageRating;

    private Integer ratingCount;

    private Long enrolledCount;

    private Long completedCount;

    private Integer availableSeats;

    private Double completionRate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}