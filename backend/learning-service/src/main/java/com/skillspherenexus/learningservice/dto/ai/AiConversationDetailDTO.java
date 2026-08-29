package com.skillspherenexus.learningservice.dto.ai;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversationDetailDTO {

    private UUID conversationId;

    private String title;

    private UUID courseId;

    private String courseTitle;

    private UUID moduleId;

    private UUID contentId;

    private String contentTitle;

    private List<AiChatMessageDTO> messages;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
