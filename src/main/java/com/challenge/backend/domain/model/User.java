package com.challenge.backend.domain.model;

import com.challenge.backend.domain.model.enums.Role;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
@Builder
public class User {
    Long id;
    String name;
    String email;
    String passwordHash;
    Role role;
    boolean enabled;
    Instant createdAt;
    Set<String> coverageStates;

    public boolean canAccessSolicitation(Solicitation solicitation) {
        return switch (role) {
            case ADMIN -> true;
            case CLIENT -> this.id.equals(solicitation.getClientId());
            case ANALYST -> coverageStates != null && coverageStates.contains(solicitation.getState());
        };
    }

    public boolean canCreateUser(Role targetRole) {
        return role == Role.ADMIN;
    }
}