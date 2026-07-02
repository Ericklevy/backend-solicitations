package com.challenge.backend.application.service;

import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitSolicitationServiceTest {

    @Mock
    private SolicitationRepositoryPort repository;

    @Mock
    private ElasticsearchPort elasticsearch;

    @Mock
    private AuditPort audit;

    @InjectMocks
    private SubmitSolicitationService service;

    private Solicitation.SolicitationBuilder solicitationBuilder;

    @BeforeEach
    void setUp() {
        solicitationBuilder = Solicitation.builder()
                .id(1L)
                .clientId(10L)
                .status(Status.DRAFT)
                .currentStep(3)
                .serviceType(ServiceType.INSTALLATION)
                .title("Valid Title")
                .description("This is a valid description with more than 20 characters")
                .cep("01001000")
                .street("Praça da Sé")
                .number("123")
                .complement("Apto 45")
                .neighborhood("Sé")
                .city("São Paulo")
                .state("SP")
                .priority(Priority.MEDIUM)
                .preferredDate(LocalDate.now().plusDays(1))
                .estimatedValue(BigDecimal.valueOf(500))
                .termsAccepted(true);
    }

    @Test
    void shouldSubmitOwnSolicitation() {
        Solicitation solicitation = solicitationBuilder.build();
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));
        when(repository.save(any(Solicitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitation result = service.execute(1L, 10L);

        assertNotNull(result);
        assertEquals(Status.SUBMITTED, result.getStatus());
        verify(repository).save(solicitation);
        verify(elasticsearch).indexSolicitation(solicitation);
        verify(audit).log(eq("SUBMIT_SOLICITATION"), eq("10"), eq("CLIENT"), eq(1L), eq(0L), eq(true), any());
    }

    @Test
    void shouldThrowWhenSolicitationNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> service.execute(1L, 10L));
        assertEquals("Solicitation not found", exception.getMessage());
    }

    @Test
    void shouldThrowWhenNotOwner() {
        Solicitation solicitation = solicitationBuilder.clientId(99L).build();
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));

        Exception exception = assertThrows(RuntimeException.class, () -> service.execute(1L, 10L));
        assertEquals("You can only submit your own solicitations", exception.getMessage());
    }

    @Test
    void shouldThrowWhenValidationFails() {
        // incomplete step 1 (title missing)
        Solicitation solicitation = solicitationBuilder.title(null).build();
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));

        Exception exception = assertThrows(RuntimeException.class, () -> service.execute(1L, 10L));
        assertTrue(exception.getMessage().contains("Validation failed"));
    }
}
