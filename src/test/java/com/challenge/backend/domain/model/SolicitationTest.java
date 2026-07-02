package com.challenge.backend.domain.model;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitationTest {

    private Solicitation solicitation;

    @BeforeEach
    void setUp() {
        solicitation = Solicitation.builder()
                .id(1L)
                .clientId(1L)
                .status(Status.DRAFT)
                .currentStep(0)
                .build();
    }

    @Test
    void shouldSaveStep1Successfully() {
        // Given
        ServiceType serviceType = ServiceType.INSTALLATION;
        String title = "Test Title";
        String description = "This is a test description with more than 20 characters";

        // When
        var result = solicitation.saveStep1(serviceType, title, description);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(serviceType, solicitation.getServiceType());
        assertEquals(title, solicitation.getTitle());
        assertEquals(description, solicitation.getDescription());
        assertEquals(1, solicitation.getCurrentStep());
    }

    @Test
    void shouldFailToSaveStep1WithInvalidTitle() {
        // Given
        ServiceType serviceType = ServiceType.INSTALLATION;
        String title = "AB"; // Too short
        String description = "This is a test description with more than 20 characters";

        // When
        var result = solicitation.saveStep1(serviceType, title, description);

        // Then
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrors().size());
        assertEquals("title", result.getErrors().get(0).getField());
    }

    @Test
    void shouldFailToSaveStep1WithInvalidDescription() {
        // Given
        ServiceType serviceType = ServiceType.INSTALLATION;
        String title = "Valid Title";
        String description = "Short"; // Too short

        // When
        var result = solicitation.saveStep1(serviceType, title, description);

        // Then
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrors().size());
        assertEquals("description", result.getErrors().get(0).getField());
    }

    @Test
    void shouldSaveStep2Successfully() {
        // Given
        String cep = "01001000";
        String street = "Praça da Sé";
        String number = "123";
        String complement = "Apto 45";
        String neighborhood = "Sé";
        String city = "São Paulo";
        String state = "SP";

        // When
        var result = solicitation.saveStep2(cep, street, number, complement, neighborhood, city, state);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("01001000", solicitation.getCep());
        assertEquals(street, solicitation.getStreet());
        assertEquals(number, solicitation.getNumber());
        assertEquals(complement, solicitation.getComplement());
        assertEquals(2, solicitation.getCurrentStep());
    }

    @Test
    void shouldFailToSaveStep2WithInvalidCep() {
        // Given
        String cep = "123"; // Invalid
        String street = "Praça da Sé";
        String number = "123";
        String complement = "Apto 45";
        String neighborhood = "Sé";
        String city = "São Paulo";
        String state = "SP";

        // When
        var result = solicitation.saveStep2(cep, street, number, complement, neighborhood, city, state);

        // Then
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrors().size());
        assertEquals("cep", result.getErrors().get(0).getField());
    }

    @Test
    void shouldSaveStep3Successfully() {
        // Given
        Priority priority = Priority.MEDIUM;
        LocalDate preferredDate = LocalDate.now().plusDays(1);
        BigDecimal estimatedValue = BigDecimal.valueOf(500);
        boolean termsAccepted = true;

        // When
        var result = solicitation.saveStep3(priority, preferredDate, estimatedValue, termsAccepted);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(priority, solicitation.getPriority());
        assertEquals(preferredDate, solicitation.getPreferredDate());
        assertEquals(estimatedValue, solicitation.getEstimatedValue());
        assertTrue(solicitation.getTermsAccepted());
        assertEquals(3, solicitation.getCurrentStep());
    }

    @Test
    void shouldFailToSaveStep3WithPastDate() {
        // Given
        Priority priority = Priority.MEDIUM;
        LocalDate preferredDate = LocalDate.now().minusDays(1); // Past date
        BigDecimal estimatedValue = BigDecimal.valueOf(500);
        boolean termsAccepted = true;

        // When
        var result = solicitation.saveStep3(priority, preferredDate, estimatedValue, termsAccepted);

        // Then
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrors().size());
        assertEquals("preferredDate", result.getErrors().get(0).getField());
    }

    @Test
    void shouldFailToSaveStep3WithHighPriorityLowValue() {
        // Given
        Priority priority = Priority.HIGH;
        LocalDate preferredDate = LocalDate.now().plusDays(1);
        BigDecimal estimatedValue = BigDecimal.valueOf(50); // Less than 100
        boolean termsAccepted = true;

        // When
        var result = solicitation.saveStep3(priority, preferredDate, estimatedValue, termsAccepted);

        // Then
        assertTrue(result.isFailure());
        assertEquals(1, result.getErrors().size());
        assertEquals("estimatedValue", result.getErrors().get(0).getField());
    }

    @Test
    void shouldSubmitCompleteSolicitation() {
        // Given
        Solicitation complete = completeSolicitation();

        // When
        var result = complete.submit();

        // Then
        assertTrue(result.isSuccess());
        assertEquals(Status.SUBMITTED, complete.getStatus());
        assertNotNull(complete.getSubmittedAt());
    }

    @Test
    void shouldFailToSubmitIncompleteSolicitation() {
        // Given - solicitation is incomplete (no steps filled)

        // When
        var result = solicitation.submit();

        // Then
        assertTrue(result.isFailure());
        assertTrue(result.getErrors().size() >= 1);
    }

    @Test
    void shouldFailToSubmitAlreadySubmittedSolicitation() {
        // Given
        Solicitation submitted = completeSolicitation();
        submitted.submit();

        // When
        var result = submitted.submit();

        // Then
        assertTrue(result.isFailure());
        assertEquals("Solicitation must be in DRAFT status to submit",
                result.getErrors().get(0).getMessage());
    }

    @Test
    void shouldApproveSolicitationWithValidComment() {
        // Given
        Solicitation submitted = submittedSolicitation();
        String comment = "This is a valid comment with more than 10 characters";

        // When
        var result = submitted.decide(true, 2L, comment);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(Status.APPROVED, submitted.getStatus());
        assertEquals(comment, submitted.getAnalysisComment());
        assertNotNull(submitted.getAnalyzedAt());
        assertEquals(2L, submitted.getAnalyzedBy());
    }

    @Test
    void shouldRejectSolicitationWithValidComment() {
        // Given
        Solicitation submitted = submittedSolicitation();
        String comment = "This is a valid rejection comment with more than 10 characters";

        // When
        var result = submitted.decide(false, 2L, comment);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(Status.REJECTED, submitted.getStatus());
        assertEquals(comment, submitted.getAnalysisComment());
        assertNotNull(submitted.getAnalyzedAt());
        assertEquals(2L, submitted.getAnalyzedBy());
    }

    @Test
    void shouldFailToDecideWithInvalidComment() {
        // Given
        Solicitation submitted = submittedSolicitation();
        String comment = "Short"; // Too short

        // When
        var result = submitted.decide(true, 2L, comment);

        // Then
        assertTrue(result.isFailure());
        assertEquals("Comment must be between 10 and 1000 characters",
                result.getErrors().get(0).getMessage());
    }

    @Test
    void shouldFailToDecideSolicitationNotInAnalysis() {
        // Given
        Solicitation draft = Solicitation.builder()
                .id(1L)
                .clientId(1L)
                .status(Status.DRAFT)
                .build();

        // When
        var result = draft.decide(true, 2L, "Valid comment with more than 10 characters");

        // Then
        assertTrue(result.isFailure());
        assertEquals("Solicitation is not in analysis phase. Current status: DRAFT",
                result.getErrors().get(0).getMessage());
    }

    @Test
    void shouldStartAnalysisSuccessfully() {
        // Given
        Solicitation submitted = submittedSolicitation();

        // When
        var result = submitted.startAnalysis(2L);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(Status.IN_REVIEW, submitted.getStatus());
    }

    @Test
    void shouldFailToStartAnalysisIfNotSubmitted() {
        // Given
        Solicitation draft = Solicitation.builder()
                .id(1L)
                .clientId(1L)
                .status(Status.DRAFT)
                .build();

        // When
        var result = draft.startAnalysis(2L);

        // Then
        assertTrue(result.isFailure());
        assertEquals("Only SUBMITTED solicitations can start analysis",
                result.getErrors().get(0).getMessage());
    }

    // Helper methods
    private Solicitation completeSolicitation() {
        Solicitation s = Solicitation.builder()
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
                .termsAccepted(true)
                .build();
        return s;
    }

    private Solicitation submittedSolicitation() {
        Solicitation s = completeSolicitation();
        s.submit();
        return s;
    }
}