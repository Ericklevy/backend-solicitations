package com.challenge.backend.interfaces.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.interfaces.dto.response.SolicitationResponse;
import org.springframework.stereotype.Component;

@Component
public class SolicitationResponseMapper {

    public SolicitationResponse toResponse(Solicitation domain) {
        if (domain == null) return null;

        return SolicitationResponse.builder()
                .id(domain.getId())
                .clientId(domain.getClientId())
                .status(domain.getStatus())
                .currentStep(domain.getCurrentStep())
                .serviceType(domain.getServiceType())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .cep(domain.getCep())
                .street(domain.getStreet())
                .number(domain.getNumber())
                .complement(domain.getComplement())
                .neighborhood(domain.getNeighborhood())
                .city(domain.getCity())
                .state(domain.getState())
                .priority(domain.getPriority())
                .preferredDate(domain.getPreferredDate())
                .estimatedValue(domain.getEstimatedValue())
                .termsAccepted(domain.getTermsAccepted())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .submittedAt(domain.getSubmittedAt())
                .analyzedAt(domain.getAnalyzedAt())
                .analyzedBy(domain.getAnalyzedBy())
                .analysisComment(domain.getAnalysisComment())
                .build();
    }
}