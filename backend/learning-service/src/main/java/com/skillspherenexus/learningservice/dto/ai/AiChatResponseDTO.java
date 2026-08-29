package com.skillspherenexus.learningservice.dto.ai;

import com.skillspherenexus.learningservice.enums.AiSenderType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatResponseDTO {

    private UUID conversationId;

    private UUID userMessageId;

    private UUID assistantMessageId;

    private String reply;

    private AiSenderType senderType;

    private String courseTitle;

    private String lessonTitle;

    private boolean contextGrounded;

    private String providerEngine;

    private List<String> suggestedFollowUps;

    private LocalDateTime timestamp;
}
