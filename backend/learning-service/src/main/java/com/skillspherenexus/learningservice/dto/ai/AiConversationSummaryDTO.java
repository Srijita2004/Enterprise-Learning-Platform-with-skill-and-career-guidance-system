package com.skillspherenexus.learningservice.dto.ai;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversationSummaryDTO {

    private UUID conversationId;

    private String title;

    private UUID courseId;

    private String courseTitle;

    private UUID contentId;

    private String contentTitle;

    private String lastMessagePreview;

    private long messageCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
