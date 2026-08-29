package com.skillspherenexus.learningservice.service.ai;

import com.skillspherenexus.learningservice.dto.ai.*;

import java.util.List;
import java.util.UUID;

public interface AiLearningAssistantService {

    AiChatResponseDTO sendMessage(AiChatRequestDTO request, UUID authenticatedUserId);

    List<AiConversationSummaryDTO> getUserConversations(UUID authenticatedUserId);

    AiConversationDetailDTO getConversationDetail(UUID conversationId, UUID authenticatedUserId);

    void deleteConversation(UUID conversationId, UUID authenticatedUserId);

    void clearAllConversations(UUID authenticatedUserId);
}
