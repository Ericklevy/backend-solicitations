package com.challenge.backend.application.service;

import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyzeSolicitationServiceTest {

    @Mock
    private SolicitationRepositoryPort repository;

    @Mock
    private ElasticsearchPort elasticsearch;

    @Mock
    private AuditPort audit;

    @InjectMocks
    private AnalyzeSolicitationService service;

    private Solicitation solicitation;

    @BeforeEach
    void setUp() {
        solicitation = Solicitation.builder()
                .id(1L)
                .clientId(10L)
                .status(Status.SUBMITTED)
                .state("SP")
                .build();
    }

    @Test
    void shouldStartAnalysis() {
        when(repository.findById(1L)).thenReturn(Optional.of(solicitation));
        when(repository.save(any(Solicitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitation result = service.startAnalysis(1L, 2L);

        assertNotNull(result);
        assertEquals(Status.IN_REVIEW, result.getStatus());
        verify(repository).save(solicitation);
        verify(elasticsearch).indexSolicitation(solicitation);
        verify(audit).log(eq("START_ANALYSIS"), eq("2"), eq("ANALYST"), eq(1L), eq(0L), eq(true), any());
    }

    @Test
    void shouldThrowWhenSolicitationNotFoundForStartAnalysis() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.startAnalysis(1L, 2L));
    }

    @Test
    void shouldThrowWhenStartAnalysisFails() {
        // status is already APPROVED, cannot start analysis
        Solicitation approved = Solicitation.builder()
                .id(1L)
                .status(Status.APPROVED)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(approved));
        assertThrows(RuntimeException.class, () -> service.startAnalysis(1L, 2L));
    }

    @Test
    void shouldDecideApprove() {
        Solicitation inReview = Solicitation.builder()
                .id(1L)
                .status(Status.IN_REVIEW)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(inReview));
        when(repository.save(any(Solicitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitation result = service.decide(1L, 2L, true, "Valid comment with more than 10 characters");

        assertNotNull(result);
        assertEquals(Status.APPROVED, result.getStatus());
        assertEquals("Valid comment with more than 10 characters", result.getAnalysisComment());
        verify(repository).save(inReview);
        verify(elasticsearch).indexSolicitation(inReview);
        verify(audit).log(eq("DECIDE_SOLICITATION"), eq("2"), eq("ANALYST"), eq(1L), eq(0L), eq(true), any());
    }

    @Test
    void shouldDecideReject() {
        Solicitation inReview = Solicitation.builder()
                .id(1L)
                .status(Status.IN_REVIEW)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(inReview));
        when(repository.save(any(Solicitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitation result = service.decide(1L, 2L, false, "Valid comment with more than 10 characters");

        assertNotNull(result);
        assertEquals(Status.REJECTED, result.getStatus());
    }

    @Test
    void shouldThrowWhenSolicitationNotFoundForDecide() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.decide(1L, 2L, true, "Valid comment"));
    }

    @Test
    void shouldThrowWhenDecideFails() {
        Solicitation draft = Solicitation.builder()
                .id(1L)
                .status(Status.DRAFT)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        assertThrows(RuntimeException.class, () -> service.decide(1L, 2L, true, "Valid comment"));
    }
}
