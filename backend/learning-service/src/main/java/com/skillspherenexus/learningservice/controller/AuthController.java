package com.skillspherenexus.learningservice.controller;

import com.skillspherenexus.learningservice.dto.UserLoginRequestDTO;
import com.skillspherenexus.learningservice.dto.UserResponseDTO;
import com.skillspherenexus.learningservice.enums.UserRole;
import com.skillspherenexus.learningservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/auth/login")
    public ResponseEntity<UserResponseDTO> login(
            @Valid @RequestBody UserLoginRequestDTO request
    ) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getUsers(
            @RequestParam(required = false) UserRole role,
            @RequestHeader(value = "X-User-Role", required = false) String requesterRole
    ) {
        requireManagerRole(requesterRole);
        return ResponseEntity.ok(userService.getUsers(role));
    }

    private void requireManagerRole(String role) {
        String normalized = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (!normalized.equals("ADMIN") && !normalized.equals("HR")) {
            throw new IllegalArgumentException("Only Admin or HR can access user management data");
        }
    }
}
