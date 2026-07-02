package com.challenge.backend.application.service;

import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSolicitationServiceTest {

    @Mock
    private SolicitationRepositoryPort repository;

    @Mock
    private ElasticsearchPort elasticsearch;

    @Mock
    private AuditPort audit;

    @InjectMocks
    private CreateSolicitationService service;

    @Test
    void shouldCreateDraftSolicitation() {
        Long clientId = 10L;
        Solicitation savedMock = Solicitation.builder()
                .id(1L)
                .clientId(clientId)
                .status(Status.DRAFT)
                .currentStep(0)
                .build();

        when(repository.save(any(Solicitation.class))).thenReturn(savedMock);

        Solicitation result = service.execute(clientId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(clientId, result.getClientId());
        assertEquals(Status.DRAFT, result.getStatus());
        assertEquals(0, result.getCurrentStep());

        // Verificando salvamento no repositório
        ArgumentCaptor<Solicitation> captor = ArgumentCaptor.forClass(Solicitation.class);
        verify(repository).save(captor.capture());
        Solicitation captured = captor.getValue();
        assertEquals(clientId, captured.getClientId());
        assertEquals(Status.DRAFT, captured.getStatus());
        assertNotNull(captured.getCreatedAt());
        assertNotNull(captured.getUpdatedAt());

        // Verificando indexação
        verify(elasticsearch).indexSolicitation(savedMock);

        // Verificando log de auditoria
        verify(audit).log(eq("CREATE_SOLICITATION"), eq("10"), eq("CLIENT"), eq(1L), eq(0L), eq(true), any());
    }
}
