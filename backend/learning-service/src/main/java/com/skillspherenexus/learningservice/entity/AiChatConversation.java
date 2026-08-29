package com.skillspherenexus.learningservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "ai_chat_conversations",
        indexes = {
                @Index(name = "idx_ai_conv_owner", columnList = "owner_user_id"),
                @Index(name = "idx_ai_conv_course", columnList = "course_id"),
                @Index(name = "idx_ai_conv_updated", columnList = "updated_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "course_title", length = 200)
    private String courseTitle;

    @Column(name = "module_id")
    private UUID moduleId;

    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "content_title", length = 200)
    private String contentTitle;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<AiChatMessage> messages = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.title == null || this.title.isBlank()) {
            this.title = "Learning Conversation";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
