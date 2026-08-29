package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.ContentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "course_contents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_module_content_order",
                        columnNames = {
                                "module_id",
                                "content_order"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseContent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID contentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "module_id",
            nullable = false
    )
    private CourseModule courseModule;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContentType contentType;

    @Column(length = 1000)
    private String contentUrl;

    @Column(columnDefinition = "TEXT")
    private String textContent;

    private Integer durationMinutes;

    @Column(
            name = "content_order",
            nullable = false
    )
    private Integer contentOrder;

    @Column(nullable = false)
    private Boolean mandatory;

    @Column(nullable = false)
    private Boolean previewAvailable;

    @Column(nullable = false)
    private Boolean published;

    private LocalDateTime availableFrom;

    private LocalDateTime availableUntil;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime currentTime = LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (mandatory == null) {
            mandatory = true;
        }

        if (previewAvailable == null) {
            previewAvailable = false;
        }

        if (published == null) {
            published = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}