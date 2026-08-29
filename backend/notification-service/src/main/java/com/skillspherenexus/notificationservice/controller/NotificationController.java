package com.skillspherenexus.notificationservice.controller;

import com.skillspherenexus.notificationservice.dto.NotificationCreateRequest;
import com.skillspherenexus.notificationservice.dto.NotificationResponse;
import com.skillspherenexus.notificationservice.enums.TargetRole;
import com.skillspherenexus.notificationservice.service.NotificationService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Returns notifications visible to the currently logged-in role.
     *
     * ADMIN  -> ADMINISTRATOR + ALL
     * HR     -> HR_MANAGER + ALL
     * LEARNER -> LEARNER + ALL
     */
    @GetMapping
    public List<NotificationResponse> list(
            @RequestParam(required = false) Boolean read,
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {

        TargetRole role = convertRole(userRole);

        return notificationService.listForRole(role, read);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {

        TargetRole role = convertRole(userRole);

        return Map.of(
                "unreadCount",
                notificationService.unreadCountForRole(role)
        );
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(
            @Valid @RequestBody NotificationCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.create(request));
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markRead(
            @PathVariable UUID notificationId
    ) {

        return notificationService.markRead(notificationId);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllRead(
            @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {

        TargetRole role = convertRole(userRole);

        return Map.of(
                "markedRead",
                notificationService.markAllReadForRole(role)
        );
    }

    private TargetRole convertRole(String role) {

        if (role == null || role.isBlank()) {
            return TargetRole.ALL;
        }

        return switch (role.trim().toUpperCase()) {

            case "ADMIN", "ADMINISTRATOR" ->
                    TargetRole.ADMINISTRATOR;

            case "HR", "HR_MANAGER" ->
                    TargetRole.HR_MANAGER;

            case "EMPLOYEE" ->
                    TargetRole.EMPLOYEE;

            case "LEARNER" ->
                    TargetRole.LEARNER;

            default ->
                    TargetRole.ALL;
        };
    }
}