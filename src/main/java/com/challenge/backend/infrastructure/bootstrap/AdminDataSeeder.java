package com.challenge.backend.infrastructure.bootstrap;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminDataSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final SecurityPort securityPort;

    @Override
    public void run(String... args) {
        try {
            String adminEmail = "admin@system.com";
            Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);

            if (existingAdmin.isEmpty()) {
                log.info("🚀 Seeding initial ADMIN user...");
                User admin = User.builder()
                        .name("Administrator")
                        .email(adminEmail)
                        .passwordHash(securityPort.encodePassword("Admin@123"))
                        .role(Role.ADMIN)
                        .enabled(true)
                        .createdAt(Instant.now())
                        .build();

                userRepository.save(admin);
                log.info("✅ Admin user seeded successfully! Email: {} | Password: Admin@123", adminEmail);
            } else {
                log.info("✅ Admin user already exists. Skipping seed.");
            }
        } catch (Exception e) {
            log.error("⚠️ Failed to seed admin user. Error: {}", e.getMessage(), e);
        }
    }
}
