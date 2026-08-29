package com.skillspherenexus.learningservice.service.ai.impl;

import com.skillspherenexus.learningservice.dto.ai.*;
import com.skillspherenexus.learningservice.entity.*;
import com.skillspherenexus.learningservice.enums.AiSenderType;
import com.skillspherenexus.learningservice.exception.ResourceNotFoundException;
import com.skillspherenexus.learningservice.repository.*;
import com.skillspherenexus.learningservice.service.ai.AiLearningAssistantService;
import com.skillspherenexus.learningservice.service.ai.AiProviderClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AiLearningAssistantServiceImpl implements AiLearningAssistantService {

    private static final Logger log = LoggerFactory.getLogger(AiLearningAssistantServiceImpl.class);

    private final AiChatConversationRepository conversationRepository;
    private final AiChatMessageRepository messageRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CourseContentRepository contentRepository;
    private final AiProviderClient aiProviderClient;

    @Override
    public AiChatResponseDTO sendMessage(AiChatRequestDTO request, UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AccessDeniedException("Authentication required to access the AI Learning Assistant");
        }

        if (request == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be blank");
        }

        // 1. Resolve or Create Conversation with Strict Ownership
        AiChatConversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + request.getConversationId()));

            // HARD SECURITY CHECK: Only the conversation owner can send messages
            if (!conversation.getOwnerUserId().equals(authenticatedUserId)) {
                log.warn("Security Alert: User {} attempted to access conversation {} owned by {}",
                        authenticatedUserId, conversation.getConversationId(), conversation.getOwnerUserId());
                throw new AccessDeniedException("Access denied: You do not own this conversation");
            }
        } else {
            String initialTitle = generateConversationTitle(request.getMessage(), request.getCourseTitle(), request.getContentTitle());
            conversation = AiChatConversation.builder()
                    .ownerUserId(authenticatedUserId)
                    .courseId(request.getCourseId())
                    .courseTitle(request.getCourseTitle())
                    .moduleId(request.getModuleId())
                    .contentId(request.getContentId())
                    .contentTitle(request.getContentTitle())
                    .title(initialTitle)
                    .build();
            conversation = conversationRepository.save(conversation);
        }

        // 2. Fetch and verify Course / Lesson Context if applicable
        String resolvedCourseTitle = request.getCourseTitle() != null ? request.getCourseTitle() : conversation.getCourseTitle();
        String resolvedModuleTitle = request.getModuleTitle();
        String resolvedLessonTitle = request.getContentTitle() != null ? request.getContentTitle() : conversation.getContentTitle();
        String resolvedLessonContent = null;

        if (request.getCourseId() != null) {
            Course course = courseRepository.findById(request.getCourseId()).orElse(null);
            if (course != null) {
                resolvedCourseTitle = course.getTitle();
                conversation.setCourseId(course.getCourseId());
                conversation.setCourseTitle(course.getTitle());
            }
        }

        if (request.getContentId() != null) {
            CourseContent content = contentRepository.findById(request.getContentId()).orElse(null);
            if (content != null) {
                resolvedLessonTitle = content.getTitle();
                resolvedLessonContent = content.getTextContent() != null ? content.getTextContent() : content.getDescription();
                conversation.setContentId(content.getContentId());
                conversation.setContentTitle(content.getTitle());

                if (content.getCourseModule() != null) {
                    resolvedModuleTitle = content.getCourseModule().getTitle();
                    conversation.setModuleId(content.getCourseModule().getModuleId());
                }
            }
        } else if (request.getModuleId() != null) {
            CourseModule module = moduleRepository.findById(request.getModuleId()).orElse(null);
            if (module != null) {
                resolvedModuleTitle = module.getTitle();
                conversation.setModuleId(module.getModuleId());
            }
        }

        // 3. Save the incoming User Message
        AiChatMessage userMessage = AiChatMessage.builder()
                .conversation(conversation)
                .senderType(AiSenderType.USER)
                .content(request.getMessage().trim())
                .contextSummary(resolvedLessonTitle != null ? "Context: " + resolvedLessonTitle : null)
                .build();
        userMessage = messageRepository.save(userMessage);

        // 4. Retrieve conversation history for context (up to last 6 messages)
        List<AiChatMessage> historyMessages = messageRepository
                .findByConversation_ConversationIdAndConversation_OwnerUserIdOrderByCreatedAtAsc(
                        conversation.getConversationId(), authenticatedUserId);

        List<Map<String, String>> historyList = new ArrayList<>();
        for (AiChatMessage m : historyMessages) {
            historyList.add(Map.of(
                    "role", m.getSenderType().name(),
                    "content", m.getContent()
            ));
        }

        // 5. Generate AI Provider Response
        AiProviderClient.ProviderResult result = aiProviderClient.generateLearningResponse(
                request.getMessage().trim(),
                request.getQuickAction(),
                resolvedCourseTitle,
                resolvedModuleTitle,
                resolvedLessonTitle,
                resolvedLessonContent,
                historyList
        );

        // 6. Save the Assistant Message
        AiChatMessage assistantMessage = AiChatMessage.builder()
                .conversation(conversation)
                .senderType(AiSenderType.ASSISTANT)
                .content(result.reply())
                .contextSummary("Engine: " + result.providerEngine())
                .build();
        assistantMessage = messageRepository.save(assistantMessage);

        // 7. Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // 8. Return response DTO
        return AiChatResponseDTO.builder()
                .conversationId(conversation.getConversationId())
                .userMessageId(userMessage.getMessageId())
                .assistantMessageId(assistantMessage.getMessageId())
                .reply(result.reply())
                .senderType(AiSenderType.ASSISTANT)
                .courseTitle(resolvedCourseTitle)
                .lessonTitle(resolvedLessonTitle)
                .contextGrounded(resolvedLessonContent != null && !resolvedLessonContent.isBlank())
                .providerEngine(result.providerEngine())
                .suggestedFollowUps(result.suggestedFollowUps())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiConversationSummaryDTO> getUserConversations(UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AccessDeniedException("Authentication required");
        }

        List<AiChatConversation> conversations = conversationRepository
                .findByOwnerUserIdOrderByUpdatedAtDesc(authenticatedUserId);

        return conversations.stream().map(c -> {
            List<AiChatMessage> msgs = messageRepository.findByConversation_ConversationIdOrderByCreatedAtAsc(c.getConversationId());
            String lastMsg = msgs.isEmpty() ? "" : msgs.get(msgs.size() - 1).getContent();
            if (lastMsg.length() > 80) {
                lastMsg = lastMsg.substring(0, 77) + "...";
            }

            return AiConversationSummaryDTO.builder()
                    .conversationId(c.getConversationId())
                    .title(c.getTitle())
                    .courseId(c.getCourseId())
                    .courseTitle(c.getCourseTitle())
                    .contentId(c.getContentId())
                    .contentTitle(c.getContentTitle())
                    .lastMessagePreview(lastMsg)
                    .messageCount(msgs.size())
                    .createdAt(c.getCreatedAt())
                    .updatedAt(c.getUpdatedAt())
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AiConversationDetailDTO getConversationDetail(UUID conversationId, UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AccessDeniedException("Authentication required");
        }

        AiChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        // HARD SECURITY CHECK: Ownership validation
        if (!conversation.getOwnerUserId().equals(authenticatedUserId)) {
            log.warn("Security Alert: User {} attempted to read conversation {} owned by {}",
                    authenticatedUserId, conversation.getConversationId(), conversation.getOwnerUserId());
            throw new AccessDeniedException("Access denied: You do not own this conversation");
        }

        List<AiChatMessage> messages = messageRepository
                .findByConversation_ConversationIdAndConversation_OwnerUserIdOrderByCreatedAtAsc(
                        conversationId, authenticatedUserId);

        List<AiChatMessageDTO> messageDTOs = messages.stream().map(m ->
                AiChatMessageDTO.builder()
                        .messageId(m.getMessageId())
                        .conversationId(conversationId)
                        .senderType(m.getSenderType())
                        .content(m.getContent())
                        .contextSummary(m.getContextSummary())
                        .createdAt(m.getCreatedAt())
                        .build()
        ).toList();

        return AiConversationDetailDTO.builder()
                .conversationId(conversation.getConversationId())
                .title(conversation.getTitle())
                .courseId(conversation.getCourseId())
                .courseTitle(conversation.getCourseTitle())
                .moduleId(conversation.getModuleId())
                .contentId(conversation.getContentId())
                .contentTitle(conversation.getContentTitle())
                .messages(messageDTOs)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    @Override
    public void deleteConversation(UUID conversationId, UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AccessDeniedException("Authentication required");
        }

        AiChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + conversationId));

        // HARD SECURITY CHECK: Ownership validation
        if (!conversation.getOwnerUserId().equals(authenticatedUserId)) {
            log.warn("Security Alert: User {} attempted to delete conversation {} owned by {}",
                    authenticatedUserId, conversation.getConversationId(), conversation.getOwnerUserId());
            throw new AccessDeniedException("Access denied: You do not own this conversation");
        }

        conversationRepository.delete(conversation);
    }

    @Override
    public void clearAllConversations(UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new AccessDeniedException("Authentication required");
        }

        conversationRepository.deleteByOwnerUserId(authenticatedUserId);
    }

    private String generateConversationTitle(String firstMessage, String courseTitle, String lessonTitle) {
        if (lessonTitle != null && !lessonTitle.isBlank()) {
            return lessonTitle;
        }
        if (firstMessage != null && !firstMessage.isBlank()) {
            String trimmed = firstMessage.trim().replaceAll("\\s+", " ");
            return trimmed.length() > 40 ? trimmed.substring(0, 37) + "..." : trimmed;
        }
        if (courseTitle != null && !courseTitle.isBlank()) {
            return courseTitle + " Q&A";
        }
        return "Learning Chat";
    }
}
