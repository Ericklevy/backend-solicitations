package com.challenge.backend.application.service;

import com.challenge.backend.application.port.out.SecurityPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.domain.service.SolicitationDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopulateSystemServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private SolicitationDomainService domainService;

    @Mock
    private SecurityPort securityPort;

    @InjectMocks
    private PopulateSystemService service;

    @BeforeEach
    void setUp() {
        lenient().when(securityPort.encodePassword(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void shouldPopulateSystemSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        AtomicLong userIdCounter = new AtomicLong(1L);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = User.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(user, userIdCounter.getAndIncrement());
            } catch (Exception e) {}
            return user;
        });

        AtomicLong solIdCounter = new AtomicLong(1L);
        when(domainService.save(any(Solicitation.class))).thenAnswer(invocation -> {
            Solicitation sol = invocation.getArgument(0);
            try {
                java.lang.reflect.Field idField = Solicitation.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(sol, solIdCounter.getAndIncrement());
            } catch (Exception e) {}
            return sol;
        });

        // Act
        Map<String, Object> result = service.execute();

        // Assert
        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("analysts_created"));
        assertEquals(8, result.get("clients_created"));
        assertEquals(20, result.get("solicitations_created"));
        
        verify(userRepository, times(10)).save(any(User.class));
        verify(domainService, atLeast(20)).save(any(Solicitation.class));
    }
    
    @Test
    void shouldHandleExceptionsAndReturnFailure() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB error"));
        
        // Act
        Map<String, Object> result = service.execute();
        
        // Assert
        assertFalse((Boolean) result.get("success"));
        assertEquals("DB error", result.get("error"));
    }
}
