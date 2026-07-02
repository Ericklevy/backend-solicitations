package com.challenge.backend.infrastructure.audit.service;

import com.challenge.backend.infrastructure.audit.entity.AuditLogEntity;
import com.challenge.backend.infrastructure.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository repository;

    @InjectMocks
    private AuditService service;

    @Test
    void shouldSaveAuditLog() {
        service.log("CREATE", "10", "CLIENT", 1L, 150L, true, null);

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(repository).save(captor.capture());

        AuditLogEntity captured = captor.getValue();
        assertEquals("CREATE", captured.getAction());
        assertEquals("10", captured.getUserId());
        assertEquals("CLIENT", captured.getUserRole());
        assertEquals(1L, captured.getEntityId());
        assertEquals(150L, captured.getDurationMs());
        assertTrue(captured.isSuccess());
        assertNull(captured.getErrorMessage());
        assertNotNull(captured.getTimestamp());
    }

    @Test
    void shouldSaveAuditLogWithErrorMessage() {
        service.log("CREATE", null, null, 1L, 150L, false, "error message");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(repository).save(captor.capture());

        AuditLogEntity captured = captor.getValue();
        assertEquals("anonymous", captured.getUserId());
        assertEquals("N/A", captured.getUserRole());
        assertFalse(captured.isSuccess());
        assertEquals("error message", captured.getErrorMessage());
    }

    @Test
    void shouldNotThrowWhenSaveFails() {
        when(repository.save(any(AuditLogEntity.class))).thenThrow(new RuntimeException("Database error"));

        // Should handle silently and not throw exception
        assertDoesNotThrow(() -> service.log("CREATE", "10", "CLIENT", 1L, 150L, true, null));
    }
}
