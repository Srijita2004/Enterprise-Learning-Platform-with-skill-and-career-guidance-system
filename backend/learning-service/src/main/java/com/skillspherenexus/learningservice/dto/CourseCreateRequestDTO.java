package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.CourseLevel;
import com.skillspherenexus.learningservice.enums.CoursePricingType;
import com.skillspherenexus.learningservice.enums.CourseType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseCreateRequestDTO {

    @NotBlank(message = "Course code is required")
    @Size(max = 30, message = "Course code cannot exceed 30 characters")
    private String courseCode;

    @NotBlank(message = "Course title is required")
    @Size(max = 150, message = "Course title cannot exceed 150 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @NotBlank(message = "Course category is required")
    @Size(max = 80, message = "Course category cannot exceed 80 characters")
    private String category;

    @NotNull(message = "Course type is required")
    private CourseType courseType;

    @NotNull(message = "Course level is required")
    private CourseLevel courseLevel;

    @NotNull(message = "Course pricing type is required")
    private CoursePricingType pricingType;

    @NotNull(message = "Course price is required")
    @DecimalMin(
            value = "0.00",
            message = "Course price cannot be negative"
    )
    @Digits(
            integer = 8,
            fraction = 2,
            message = "Course price can have maximum 8 digits and 2 decimal places"
    )
    private BigDecimal price;

    @NotBlank(message = "Currency code is required")
    @Pattern(
            regexp = "^[A-Za-z]{3}$",
            message = "Currency code must contain exactly 3 letters"
    )
    private String currencyCode;

    @NotBlank(message = "Instructor name is required")
    @Size(max = 100, message = "Instructor name cannot exceed 100 characters")
    private String instructorName;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 1000, message = "Duration cannot exceed 1000 hours")
    private Integer durationHours;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 1, message = "Maximum capacity must be at least 1")
    private Integer maxCapacity;

    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score cannot be below 0")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    private Integer passingScore;

    @NotNull(message = "Certificate preference is required")
    private Boolean certificateEnabled;

    private LocalDate startDate;

    private LocalDate endDate;
}