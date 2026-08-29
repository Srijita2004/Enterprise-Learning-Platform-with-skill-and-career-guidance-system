package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.UserRole;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {
    private UUID userId;
    private String fullName;
    private String email;
    private UserRole role;
    private Boolean active;
    private String token;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
