package com.challenge.backend.infrastructure.search.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.infrastructure.search.document.SolicitationDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SolicitationDocumentMapperTest {

    private final SolicitationDocumentMapper mapper = new SolicitationDocumentMapper();

    @Test
    void shouldMapDomainToDocument() {
        Instant now = Instant.now();
        Solicitation domain = Solicitation.builder()
                .id(1L)
                .clientId(10L)
                .status(Status.DRAFT)
                .serviceType(ServiceType.INSTALLATION)
                .title("Title")
                .description("Description")
                .state("SP")
                .city("São Paulo")
                .priority(Priority.HIGH)
                .createdAt(now)
                .submittedAt(now)
                .build();

        SolicitationDocument doc = mapper.toDocument(domain);

        assertNotNull(doc);
        assertEquals("1", doc.getId());
        assertEquals(1L, doc.getSolicitationId());
        assertEquals(10L, doc.getClientId());
        assertEquals("DRAFT", doc.getStatus());
        assertEquals("INSTALLATION", doc.getServiceType());
        assertEquals("Title", doc.getTitle());
        assertEquals("Description", doc.getDescription());
        assertEquals("SP", doc.getState());
        assertEquals("São Paulo", doc.getCity());
        assertEquals("HIGH", doc.getPriority());
        assertEquals(now, doc.getCreatedAt());
        assertEquals(now, doc.getSubmittedAt());
    }

    @Test
    void shouldMapDocumentToDomain() {
        SolicitationDocument doc = SolicitationDocument.builder()
                .solicitationId(1L)
                .clientId(10L)
                .build();

        Solicitation domain = mapper.toDomain(doc);

        assertNotNull(domain);
        assertEquals(1L, domain.getId());
        assertEquals(10L, domain.getClientId());
    }

    @Test
    void shouldHandleNullEnums() {
        Solicitation domain = Solicitation.builder()
                .id(1L)
                .status(null)
                .serviceType(null)
                .priority(null)
                .build();

        SolicitationDocument doc = mapper.toDocument(domain);

        assertNotNull(doc);
        assertNull(doc.getStatus());
        assertNull(doc.getServiceType());
        assertNull(doc.getPriority());
    }

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(mapper.toDocument(null));
        assertNull(mapper.toDomain(null));
    }
}
