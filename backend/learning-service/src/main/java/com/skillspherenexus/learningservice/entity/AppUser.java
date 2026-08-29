package com.skillspherenexus.learningservice.entity;

import com.skillspherenexus.learningservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "app_users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_app_user_name_role",
                        columnNames = {"normalized_name", "role"}
                ),
                @UniqueConstraint(
                        name = "uk_app_user_email",
                        columnNames = {"normalized_email"}
                )
        },
        indexes = {
                @Index(name = "idx_app_user_role", columnList = "role"),
                @Index(name = "idx_app_user_normalized_name", columnList = "normalized_name"),
                @Index(name = "idx_app_user_normalized_email", columnList = "normalized_email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "normalized_name", nullable = false, length = 150)
    private String normalizedName;

    /**
     * Nullable only to support existing project databases created before
     * email/password authentication was added. The next successful sign-in
     * securely attaches credentials to that legacy account.
     */
    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "normalized_email", length = 320)
    private String normalizedEmail;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (userId == null) {
            userId = UUID.randomUUID();
        }
        if (active == null) {
            active = true;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
