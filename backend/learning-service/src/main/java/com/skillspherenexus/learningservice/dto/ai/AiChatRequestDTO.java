package com.skillspherenexus.learningservice.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatRequestDTO {

    private UUID conversationId;

    private UUID courseId;

    private String courseTitle;

    private UUID moduleId;

    private String moduleTitle;

    private UUID contentId;

    private String contentTitle;

    @NotBlank(message = "Message content cannot be blank")
    @Size(max = 3000, message = "Message exceeds maximum allowed length of 3000 characters")
    private String message;

    private String quickAction;
}
