package com.challenge.backend.interfaces.dto.response;

import com.challenge.backend.domain.model.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;
    private Instant createdAt;
    private Set<String> coverageStates;
}