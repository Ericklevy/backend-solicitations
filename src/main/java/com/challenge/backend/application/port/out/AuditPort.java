package com.challenge.backend.application.port.out;

public interface AuditPort {
    void log(String action, String userId, String userRole, Long entityId,
             long durationMs, boolean success, String errorMessage);
}