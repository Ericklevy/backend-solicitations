package com.challenge.backend.infrastructure.audit.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_role")
    private String userRole;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(nullable = false)
    private boolean success;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private Instant timestamp;
}