package com.challenge.backend.interfaces.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.interfaces.dto.response.SolicitationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitationResponseMapperTest {

    private final SolicitationResponseMapper mapper = new SolicitationResponseMapper();

    @Test
    void shouldMapDomainToResponse() {
        Instant now = Instant.now();
        LocalDate preferredDate = LocalDate.now().plusDays(2);
        Solicitation domain = Solicitation.builder()
                .id(1L)
                .clientId(10L)
                .status(Status.DRAFT)
                .currentStep(2)
                .serviceType(ServiceType.INSTALLATION)
                .title("Title")
                .description("Description")
                .cep("01001000")
                .street("Street")
                .number("12")
                .complement("Complement")
                .neighborhood("Neighborhood")
                .city("City")
                .state("SP")
                .priority(Priority.HIGH)
                .preferredDate(preferredDate)
                .estimatedValue(BigDecimal.valueOf(150))
                .termsAccepted(true)
                .createdAt(now)
                .updatedAt(now)
                .submittedAt(now)
                .analyzedAt(now)
                .analyzedBy(2L)
                .analysisComment("Comment")
                .build();

        SolicitationResponse response = mapper.toResponse(domain);

        assertNotNull(response);
        assertEquals(domain.getId(), response.getId());
        assertEquals(domain.getClientId(), response.getClientId());
        assertEquals(domain.getStatus(), response.getStatus());
        assertEquals(domain.getCurrentStep(), response.getCurrentStep());
        assertEquals(domain.getServiceType(), response.getServiceType());
        assertEquals(domain.getTitle(), response.getTitle());
        assertEquals(domain.getDescription(), response.getDescription());
        assertEquals(domain.getCep(), response.getCep());
        assertEquals(domain.getStreet(), response.getStreet());
        assertEquals(domain.getNumber(), response.getNumber());
        assertEquals(domain.getComplement(), response.getComplement());
        assertEquals(domain.getNeighborhood(), response.getNeighborhood());
        assertEquals(domain.getCity(), response.getCity());
        assertEquals(domain.getState(), response.getState());
        assertEquals(domain.getPriority(), response.getPriority());
        assertEquals(domain.getPreferredDate(), response.getPreferredDate());
        assertEquals(domain.getEstimatedValue(), response.getEstimatedValue());
        assertEquals(domain.getTermsAccepted(), response.getTermsAccepted());
        assertEquals(domain.getCreatedAt(), response.getCreatedAt());
        assertEquals(domain.getUpdatedAt(), response.getUpdatedAt());
        assertEquals(domain.getSubmittedAt(), response.getSubmittedAt());
        assertEquals(domain.getAnalyzedAt(), response.getAnalyzedAt());
        assertEquals(domain.getAnalyzedBy(), response.getAnalyzedBy());
        assertEquals(domain.getAnalysisComment(), response.getAnalysisComment());
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(mapper.toResponse(null));
    }
}
