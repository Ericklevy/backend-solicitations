package com.challenge.backend.infrastructure.persistence.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.infrastructure.persistence.entity.SolicitationEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SolicitationMapperTest {

    private final SolicitationMapper mapper = new SolicitationMapper();

    @Test
    void shouldMapEntityToDomain() {
        Instant now = Instant.now();
        LocalDate preferredDate = LocalDate.now().plusDays(2);
        SolicitationEntity entity = SolicitationEntity.builder()
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
                .build();

        Solicitation domain = mapper.toDomain(entity);

        assertNotNull(domain);
        assertEquals(entity.getId(), domain.getId());
        assertEquals(entity.getClientId(), domain.getClientId());
        assertEquals(entity.getStatus(), domain.getStatus());
        assertEquals(entity.getCurrentStep(), domain.getCurrentStep());
        assertEquals(entity.getServiceType(), domain.getServiceType());
        assertEquals(entity.getTitle(), domain.getTitle());
        assertEquals(entity.getDescription(), domain.getDescription());
        assertEquals(entity.getCep(), domain.getCep());
        assertEquals(entity.getStreet(), domain.getStreet());
        assertEquals(entity.getNumber(), domain.getNumber());
        assertEquals(entity.getComplement(), domain.getComplement());
        assertEquals(entity.getNeighborhood(), domain.getNeighborhood());
        assertEquals(entity.getCity(), domain.getCity());
        assertEquals(entity.getState(), domain.getState());
        assertEquals(entity.getPriority(), domain.getPriority());
        assertEquals(entity.getPreferredDate(), domain.getPreferredDate());
        assertEquals(entity.getEstimatedValue(), domain.getEstimatedValue());
        assertEquals(entity.getTermsAccepted(), domain.getTermsAccepted());
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(entity.getUpdatedAt(), domain.getUpdatedAt());
    }

    @Test
    void shouldMapDomainToEntity() {
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
                .build();

        SolicitationEntity entity = mapper.toEntity(domain);

        assertNotNull(entity);
        assertEquals(domain.getId(), entity.getId());
        assertEquals(domain.getClientId(), entity.getClientId());
        assertEquals(domain.getStatus(), entity.getStatus());
        assertEquals(domain.getCurrentStep(), entity.getCurrentStep());
        assertEquals(domain.getServiceType(), entity.getServiceType());
        assertEquals(domain.getTitle(), entity.getTitle());
        assertEquals(domain.getDescription(), entity.getDescription());
        assertEquals(domain.getCep(), entity.getCep());
        assertEquals(domain.getStreet(), entity.getStreet());
        assertEquals(domain.getNumber(), entity.getNumber());
        assertEquals(domain.getComplement(), entity.getComplement());
        assertEquals(domain.getNeighborhood(), entity.getNeighborhood());
        assertEquals(domain.getCity(), entity.getCity());
        assertEquals(domain.getState(), entity.getState());
        assertEquals(domain.getPriority(), entity.getPriority());
        assertEquals(domain.getPreferredDate(), entity.getPreferredDate());
        assertEquals(domain.getEstimatedValue(), entity.getEstimatedValue());
        assertEquals(domain.getTermsAccepted(), entity.getTermsAccepted());
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
