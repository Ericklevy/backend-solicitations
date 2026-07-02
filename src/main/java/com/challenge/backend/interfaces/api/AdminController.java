package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.interfaces.dto.request.CreateUserRequest;
import com.challenge.backend.interfaces.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative endpoints")
public class AdminController {

    private final UserRepositoryPort userRepository;
    private final SecurityPort securityPort;

    @PostMapping("/users")
    @Auditable(action = "CREATE_USER")
    @Operation(summary = "Create a new internal user (ANALYST or ADMIN)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(securityPort.encodePassword(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .createdAt(Instant.now())
                .coverageStates(request.getCoverageStates() != null ?
                        new HashSet<>(request.getCoverageStates()) : null)
                .build();

        User saved = userRepository.save(user);
        return ResponseEntity.ok(toResponse(saved));
    }

    @PutMapping("/users/{id}/coverage")
    @Operation(summary = "Update analyst coverage states")
    public ResponseEntity<UserResponse> updateCoverage(
            @PathVariable Long id,
            @RequestBody Set<String> states) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.ANALYST) {
            throw new RuntimeException("Only analysts can have coverage states");
        }

        User updatedUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .coverageStates(states)
                .build();

        return ResponseEntity.ok(toResponse(userRepository.save(updatedUser)));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        // TODO: Implement pagination
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .coverageStates(user.getCoverageStates())
                .build();
    }
}