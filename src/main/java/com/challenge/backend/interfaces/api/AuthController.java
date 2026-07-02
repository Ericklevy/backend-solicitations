package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.interfaces.dto.request.AuthRequest;
import com.challenge.backend.interfaces.dto.request.RegisterRequest;
import com.challenge.backend.interfaces.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final UserRepositoryPort userRepository;
    private final SecurityPort securityPort;

    @PostMapping("/register")
    @Operation(summary = "Register a new client user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(securityPort.encodePassword(request.getPassword()))
                .role(Role.CLIENT)
                .enabled(true)
                .createdAt(Instant.now())
                .build();

        User saved = userRepository.save(user);
        String token = securityPort.generateToken(saved);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(saved.getEmail())
                .role(saved.getRole().name())
                .build());
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!securityPort.validatePassword(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        String token = securityPort.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }
}