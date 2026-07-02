package com.challenge.backend.infrastructure.audit.service;

import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.infrastructure.audit.entity.AuditLogEntity;
import com.challenge.backend.infrastructure.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService implements AuditPort {

    private final AuditLogRepository auditLogRepository;

    @Async
    @Override
    public void log(String action, String userId, String userRole, Long entityId,
                    long durationMs, boolean success, String errorMessage) {
        try {
            AuditLogEntity auditLog = AuditLogEntity.builder()
                    .action(action)
                    .userId(userId != null ? userId : "anonymous")
                    .userRole(userRole != null ? userRole : "N/A")
                    .entityId(entityId)
                    .durationMs(durationMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .timestamp(Instant.now())
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} - {}", action, userId);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}