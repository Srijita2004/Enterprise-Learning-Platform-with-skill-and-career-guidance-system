package com.skillspherenexus.careerservice.dto;

import com.skillspherenexus.careerservice.entity.NominationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobNominationDTO {
    private Long id;

    @NotNull(message = "Job ID is required")
    private Long jobId;

    private String jobTitle;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    private String employeeName;

    private String nominatedBy;

    private Integer matchScore;

    private NominationStatus status;

    private String notes;

    private LocalDateTime nominationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
