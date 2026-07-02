package com.challenge.backend.infrastructure.audit.aspect;

import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.infrastructure.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditService auditService;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startTime = System.currentTimeMillis();
        boolean success = true;
        String errorMessage = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Obter usuário autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = auth != null && auth.getName() != null ? auth.getName() : "anonymous";
            String userRole = auth != null && auth.getAuthorities() != null ?
                    auth.getAuthorities().toString() : "N/A";

            // Extrair entityId dos parâmetros
            Long entityId = extractEntityId(joinPoint);

            // Registrar auditoria
            auditService.log(
                    auditable.action(),
                    userId,
                    userRole,
                    entityId,
                    duration,
                    success,
                    errorMessage
            );

            // Log estruturado
            Map<String, Object> logData = new HashMap<>();
            logData.put("timestamp", Instant.now());
            logData.put("action", auditable.action());
            logData.put("userId", userId);
            logData.put("userRole", userRole);
            logData.put("entityId", entityId);
            logData.put("durationMs", duration);
            logData.put("success", success);
            logData.put("error", errorMessage);

            log.info("AUDIT: {}", logData);
        }
    }

    private Long extractEntityId(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
            if (arg instanceof String) {
                try {
                    return Long.parseLong((String) arg);
                } catch (NumberFormatException e) {
                    // Ignore
                }
            }
        }
        return null;
    }
}