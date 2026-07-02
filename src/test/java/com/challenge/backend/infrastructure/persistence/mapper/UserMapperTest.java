package com.challenge.backend.infrastructure.persistence.mapper;

import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void shouldMapEntityToDomain() {
        Instant now = Instant.now();
        UserEntity entity = UserEntity.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hashed")
                .role(Role.ANALYST)
                .enabled(true)
                .createdAt(now)
                .build();

        User domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getName(), domain.getName());
        assertEquals(entity.getEmail(), domain.getEmail());
        assertEquals(entity.getPasswordHash(), domain.getPasswordHash());
        assertEquals(entity.getRole(), domain.getRole());
        assertEquals(entity.isEnabled(), domain.isEnabled());
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
    }

    @Test
    void shouldMapDomainToEntity() {
        Instant now = Instant.now();
        User domain = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .passwordHash("hashed")
                .role(Role.ANALYST)
                .enabled(true)
                .createdAt(now)
                .build();

        UserEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getName(), entity.getName());
        assertEquals(domain.getEmail(), entity.getEmail());
        assertEquals(domain.getPasswordHash(), entity.getPasswordHash());
        assertEquals(domain.getRole(), entity.getRole());
        assertEquals(domain.isEnabled(), entity.isEnabled());
        assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
    }

    @Test
    void shouldReturnNullForNullEntity() {
        assertNull(mapper.toDomain(null));
    }

    @Test
    void shouldReturnNullForNullDomain() {
        assertNull(mapper.toEntity(null));
    }
}
