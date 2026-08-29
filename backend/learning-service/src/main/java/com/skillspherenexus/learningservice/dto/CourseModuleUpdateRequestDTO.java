package com.skillspherenexus.learningservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModuleUpdateRequestDTO {

    @NotBlank(message = "Module title is required")
    @Size(
            max = 150,
            message = "Module title cannot exceed 150 characters"
    )
    private String title;

    @Size(
            max = 5000,
            message = "Module description cannot exceed 5000 characters"
    )
    private String description;

    @NotNull(message = "Module order is required")
    @Min(
            value = 1,
            message = "Module order must be at least 1"
    )
    private Integer moduleOrder;
}