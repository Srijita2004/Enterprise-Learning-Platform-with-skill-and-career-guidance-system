package com.skillspherenexus.certificationmanagementservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.skillspherenexus.certificationmanagementservice.dto.NotificationResponse;
import com.skillspherenexus.certificationmanagementservice.enums.NotificationStatus;
import com.skillspherenexus.certificationmanagementservice.security.*;
import com.skillspherenexus.certificationmanagementservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@PreAuthorize("hasAnyRole('ADMIN','HR')") @RestController @RequestMapping("/api/notifications") @RequiredArgsConstructor
public class NotificationController {
    private final NotificationService service;
    @GetMapping public List<NotificationResponse> list(@RequestParam(required=false) NotificationStatus status){ return service.list(status); }
    @PostMapping("/generate") public Map<String,Integer> generate(@RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return Map.of("notificationsCreated",service.generateDueNotifications(RoleGuard.manager(userId,role))); }
    @PatchMapping("/{id}/acknowledge") public NotificationResponse acknowledge(@PathVariable UUID id,@RequestHeader(value="X-User-Id",required=false) String userId,@RequestHeader(value="X-User-Role",required=false) String role){ return service.acknowledge(id,RoleGuard.manager(userId,role)); }
}
