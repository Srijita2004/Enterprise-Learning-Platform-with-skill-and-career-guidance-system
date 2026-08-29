package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.AiChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiChatMessageRepository extends JpaRepository<AiChatMessage, UUID> {

    List<AiChatMessage> findByConversation_ConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<AiChatMessage> findByConversation_ConversationIdAndConversation_OwnerUserIdOrderByCreatedAtAsc(UUID conversationId, UUID ownerUserId);

    long countByConversation_ConversationId(UUID conversationId);
}
