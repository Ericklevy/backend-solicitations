package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.interfaces.dto.request.CreateUserRequest;
import com.challenge.backend.interfaces.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(
        summary = "01 - Create a new internal user (ANALYST or ADMIN)",
        description = "Creates an ANALYST or ADMIN user. For ANALYST, include coverageStates (array of UF codes like ['SP','RJ','MG']). The ID returned here is used in the coverage endpoint below.",
        operationId = "admin-01-createUser"
    )
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
    @Operation(
        summary = "02 - Update analyst coverage states",
        description = "Updates which states (UFs) the analyst can work on. The {id} is the USER ID of the ANALYST returned by the 'Create user' endpoint above. Example: if the analyst was created with 'id: 14', use 14 here. Body is a simple JSON array: ['SP','RJ','MG']",
        operationId = "admin-02-updateCoverage"
    )
    public ResponseEntity<UserResponse> updateCoverage(
            @Parameter(description = "ID of the ANALYST USER (returned by the Create User endpoint). Example: 14") @PathVariable Long id,
            @RequestBody com.challenge.backend.interfaces.dto.request.CoverageUpdateRequest request) {

        Set<String> states = request.getEffectiveStates();

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
    @Operation(
        summary = "03 - List all users",
        description = "Returns all registered users (clients, analysts and admins). No path parameters needed.",
        operationId = "admin-03-listUsers"
    )
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @Operation(
        summary = "04 - Get user by ID",
        description = "Returns details of a single user. The {id} is the USER ID (shown in the list above or returned when the user was created).",
        operationId = "admin-04-getUserById"
    )
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "ID of the USER to retrieve. Example: 14") @PathVariable Long id) {
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