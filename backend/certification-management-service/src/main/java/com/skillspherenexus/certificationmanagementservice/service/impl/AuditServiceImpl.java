package com.skillspherenexus.certificationmanagementservice.service.impl;

import tools.jackson.databind.json.JsonMapper;
import com.skillspherenexus.certificationmanagementservice.dto.AuditLogResponse;
import com.skillspherenexus.certificationmanagementservice.entity.AuditLog;
import com.skillspherenexus.certificationmanagementservice.enums.AuditAction;
import com.skillspherenexus.certificationmanagementservice.repository.AuditLogRepository;
import com.skillspherenexus.certificationmanagementservice.security.RequestActor;
import com.skillspherenexus.certificationmanagementservice.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository repository;
    private final JsonMapper objectMapper;

    @Override
    public void record(UUID certificationId, AuditAction action, RequestActor actor, String details, Object beforeState, Object afterState) {
        String previousHash = certificationId == null ? null : repository.findTopByCertificationIdOrderBySequenceNumberDesc(certificationId)
                .map(AuditLog::getEventHash).orElse(null);
        LocalDateTime now = LocalDateTime.now();
        String beforeJson = json(beforeState);
        String afterJson = json(afterState);
        String payload = String.join("|",
                previousHash == null ? "GENESIS" : previousHash,
                String.valueOf(certificationId), action.name(), actor == null ? "system" : actor.userId(),
                actor == null ? "SYSTEM" : actor.role(), details == null ? "" : details,
                beforeJson == null ? "" : beforeJson, afterJson == null ? "" : afterJson, now.toString());

        repository.save(AuditLog.builder()
                .auditId(UUID.randomUUID()).certificationId(certificationId).action(action)
                .actorUserId(actor == null ? "system" : actor.userId()).actorRole(actor == null ? "SYSTEM" : actor.role())
                .details(details == null ? "" : details).beforeState(beforeJson).afterState(afterJson)
                .previousHash(previousHash).eventHash(sha256(payload)).createdAt(now).build());
    }

    @Override @Transactional(readOnly = true)
    public List<AuditLogResponse> recent() { return repository.findTop200ByOrderBySequenceNumberDesc().stream().map(this::map).toList(); }

    @Override @Transactional(readOnly = true)
    public List<AuditLogResponse> byCertification(UUID certificationId) { return repository.findByCertificationIdOrderBySequenceNumberDesc(certificationId).stream().map(this::map).toList(); }

    private AuditLogResponse map(AuditLog a) {
        return new AuditLogResponse(a.getSequenceNumber(), a.getAuditId(), a.getCertificationId(), a.getAction(), a.getActorUserId(), a.getActorRole(), a.getDetails(), a.getPreviousHash(), a.getEventHash(), a.getCreatedAt());
    }
    private String json(Object value) {
        if (value == null) return null;
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception ex) { return String.valueOf(value); }
    }
    private String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("Unable to create immutable audit hash", ex); }
    }
}
