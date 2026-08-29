package com.skillspherenexus.learningservice.dto;

import com.skillspherenexus.learningservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginRequestDTO {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 320, message = "Email address cannot exceed 320 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 72, message = "Password must contain between 1 and 72 characters")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    private UUID preferredUserId;
}
