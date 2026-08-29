package com.skillspherenexus.learningservice.dto.ai;

import com.skillspherenexus.learningservice.enums.AiSenderType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatMessageDTO {

    private UUID messageId;

    private UUID conversationId;

    private AiSenderType senderType;

    private String content;

    private String contextSummary;

    private LocalDateTime createdAt;
}
