package com.challenge.backend.domain.service;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitationDomainServiceTest {

    @Mock
    private SolicitationRepositoryPort repository;

    @InjectMocks
    private SolicitationDomainService service;

    private Solicitation solicitation;

    @BeforeEach
    void setUp() {
        solicitation = Solicitation.builder()
                .id(1L)
                .clientId(2L)
                .status(Status.DRAFT)
                .build();
    }

    @Test
    void shouldFindById() {
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));
        Solicitation result = service.findById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowWhenNotFoundById() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.findById(1L));
    }

    @Test
    void shouldSave() {
        when(repository.save(solicitation)).thenReturn(solicitation);
        Solicitation result = service.save(solicitation);
        assertNotNull(result);
        verify(repository).save(solicitation);
    }

    @Test
    void shouldFindByClientId() {
        when(repository.findByClientId(2L)).thenReturn(List.of(solicitation));
        List<Solicitation> result = service.findByClientId(2L);
        assertEquals(1, result.size());
    }

    @Test
    void shouldFindByState() {
        when(repository.findByState("SP")).thenReturn(List.of(solicitation));
        List<Solicitation> result = service.findByState("SP");
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnTrueForCanEditWhenDraftAndOwner() {
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));
        assertTrue(service.canEdit(1L, 2L));
    }

    @Test
    void shouldReturnFalseForCanEditWhenNotDraft() {
        Solicitation submitted = Solicitation.builder()
                .id(1L)
                .clientId(2L)
                .status(Status.SUBMITTED)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(submitted));
        assertFalse(service.canEdit(1L, 2L));
    }

    @Test
    void shouldReturnFalseForCanEditWhenNotOwner() {
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));
        assertFalse(service.canEdit(1L, 99L));
    }
}
