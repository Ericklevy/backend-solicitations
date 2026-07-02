package com.challenge.backend.infrastructure.persistence.mapper;

import com.challenge.backend.domain.model.User;
import com.challenge.backend.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class UserMapper {
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .role(entity.getRole())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .coverageStates(entity.getCoverageStates() != null
                        ? new HashSet<>(entity.getCoverageStates())
                        : null)
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .role(domain.getRole())
                .enabled(domain.isEnabled())
                .createdAt(domain.getCreatedAt())
                .coverageStates(domain.getCoverageStates() != null
                        ? new HashSet<>(domain.getCoverageStates())
                        : new HashSet<>())
                .build();
    }
}