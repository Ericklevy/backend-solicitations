package com.challenge.backend.infrastructure.search.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.infrastructure.search.document.SolicitationDocument;
import org.springframework.stereotype.Component;

@Component
public class SolicitationDocumentMapper {

    public SolicitationDocument toDocument(Solicitation domain) {
        if (domain == null) return null;

        return SolicitationDocument.builder()
                .id(domain.getId().toString())
                .solicitationId(domain.getId())
                .clientId(domain.getClientId())
                .status(domain.getStatus() != null ? domain.getStatus().name() : null)
                .serviceType(domain.getServiceType() != null ? domain.getServiceType().name() : null)
                .title(domain.getTitle())
                .description(domain.getDescription())
                .state(domain.getState())
                .city(domain.getCity())
                .priority(domain.getPriority() != null ? domain.getPriority().name() : null)
                .createdAt(domain.getCreatedAt())
                .submittedAt(domain.getSubmittedAt())
                .build();
    }

    public Solicitation toDomain(SolicitationDocument document) {
        if (document == null) return null;
        return Solicitation.builder()
                .id(document.getSolicitationId())
                .clientId(document.getClientId())
                .build();
    }
}