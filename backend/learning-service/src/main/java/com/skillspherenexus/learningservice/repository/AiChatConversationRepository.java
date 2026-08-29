package com.skillspherenexus.learningservice.repository;

import com.skillspherenexus.learningservice.entity.AiChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiChatConversationRepository extends JpaRepository<AiChatConversation, UUID> {

    List<AiChatConversation> findByOwnerUserIdOrderByUpdatedAtDesc(UUID ownerUserId);

    Optional<AiChatConversation> findByConversationIdAndOwnerUserId(UUID conversationId, UUID ownerUserId);

    long countByOwnerUserId(UUID ownerUserId);

    void deleteByOwnerUserId(UUID ownerUserId);
}
