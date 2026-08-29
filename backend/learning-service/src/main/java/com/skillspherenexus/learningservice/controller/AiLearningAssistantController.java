package com.skillspherenexus.learningservice.controller;

import com.skillspherenexus.learningservice.dto.ai.*;
import com.skillspherenexus.learningservice.service.ai.AiLearningAssistantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for the SkillSphere AI Learning Assistant.
 * All endpoints require authentication and enforce strict user data isolation.
 */
@RestController
@RequestMapping("/api/ai-assistant")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AiLearningAssistantController {

    private final AiLearningAssistantService aiLearningAssistantService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDTO> sendMessage(
            @Valid @RequestBody AiChatRequestDTO request
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        AiChatResponseDTO response = aiLearningAssistantService.sendMessage(request, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AiConversationSummaryDTO>> getUserConversations() {
        UUID authenticatedUserId = getAuthenticatedUserId();
        List<AiConversationSummaryDTO> conversations = aiLearningAssistantService.getUserConversations(authenticatedUserId);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<AiConversationDetailDTO> getConversationDetail(
            @PathVariable UUID conversationId
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        AiConversationDetailDTO detail = aiLearningAssistantService.getConversationDetail(conversationId, authenticatedUserId);
        return ResponseEntity.ok(detail);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID conversationId
    ) {
        UUID authenticatedUserId = getAuthenticatedUserId();
        aiLearningAssistantService.deleteConversation(conversationId, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/conversations")
    public ResponseEntity<Void> clearAllConversations() {
        UUID authenticatedUserId = getAuthenticatedUserId();
        aiLearningAssistantService.clearAllConversations(authenticatedUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extracts the authenticated user's UUID strictly from the SecurityContext.
     * NEVER trusts any client-provided identity headers or payload fields.
     */
    private UUID getAuthenticatedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Authentication required to access the AI Learning Assistant");
        }

        String principal = auth.getName();
        try {
            return UUID.fromString(principal);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid authenticated user identity format: " + principal);
        }
    }
}
