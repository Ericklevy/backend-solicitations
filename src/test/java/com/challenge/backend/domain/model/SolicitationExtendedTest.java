package com.challenge.backend.domain.model;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitationExtendedTest {

    private Solicitation.SolicitationBuilder solicitationBuilder;

    @BeforeEach
    void setUp() {
        solicitationBuilder = Solicitation.builder()
                .id(1L)
                .clientId(1L)
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
    void shouldFailToSubmitWhenStep2Incomplete() {
        Solicitation solicitation = solicitationBuilder.street(null).build();
        var result = solicitation.submit();
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToSubmitWhenTermsNotAccepted() {
        Solicitation solicitation = solicitationBuilder.termsAccepted(false).build();
        var result = solicitation.submit();
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToSubmitWithHighPriorityAndLowValue() {
        Solicitation solicitation = solicitationBuilder
                .priority(Priority.HIGH)
                .estimatedValue(BigDecimal.valueOf(50))
                .build();
        var result = solicitation.submit();
        assertTrue(result.isFailure());
    }

    @Test
    void shouldStartAnalysisFromSubmittedStatus() {
        Solicitation solicitation = solicitationBuilder.status(Status.SUBMITTED).build();
        var result = solicitation.startAnalysis(2L);
        assertTrue(result.isSuccess());
        assertEquals(Status.IN_REVIEW, solicitation.getStatus());
    }

    @Test
    void shouldFailToStartAnalysisFromInReview() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        var result = solicitation.startAnalysis(2L);
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToStartAnalysisFromApproved() {
        Solicitation solicitation = solicitationBuilder.status(Status.APPROVED).build();
        var result = solicitation.startAnalysis(2L);
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToStartAnalysisFromRejected() {
        Solicitation solicitation = solicitationBuilder.status(Status.REJECTED).build();
        var result = solicitation.startAnalysis(2L);
        assertTrue(result.isFailure());
    }

    @Test
    void shouldApproveFromInReviewStatus() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        var result = solicitation.decide(true, 2L, "This is a valid comment with more than 10 characters");
        assertTrue(result.isSuccess());
        assertEquals(Status.APPROVED, solicitation.getStatus());
        assertEquals("This is a valid comment with more than 10 characters", solicitation.getAnalysisComment());
        assertEquals(2L, solicitation.getAnalyzedBy());
        assertNotNull(solicitation.getAnalyzedAt());
    }

    @Test
    void shouldRejectFromInReviewStatus() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        var result = solicitation.decide(false, 2L, "This is a valid comment with more than 10 characters");
        assertTrue(result.isSuccess());
        assertEquals(Status.REJECTED, solicitation.getStatus());
    }

    @Test
    void shouldApproveFromSubmittedStatus() {
        // canAnalyze returns true for SUBMITTED and IN_REVIEW
        Solicitation solicitation = solicitationBuilder.status(Status.SUBMITTED).build();
        var result = solicitation.decide(true, 2L, "This is a valid comment with more than 10 characters");
        assertTrue(result.isSuccess());
        assertEquals(Status.APPROVED, solicitation.getStatus());
    }

    @Test
    void shouldFailToDecideFromDraftStatus() {
        Solicitation solicitation = solicitationBuilder.status(Status.DRAFT).build();
        var result = solicitation.decide(true, 2L, "This is a valid comment with more than 10 characters");
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToDecideWithNullComment() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        var result = solicitation.decide(true, 2L, null);
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToDecideWithCommentTooShort() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        var result = solicitation.decide(true, 2L, "short");
        assertTrue(result.isFailure());
    }

    @Test
    void shouldFailToDecideWithCommentTooLong() {
        Solicitation solicitation = solicitationBuilder.status(Status.IN_REVIEW).build();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1005; i++) {
            sb.append("a");
        }
        var result = solicitation.decide(true, 2L, sb.toString());
        assertTrue(result.isFailure());
    }
}
