package com.challenge.backend.infrastructure.persistence.mapper;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.infrastructure.persistence.entity.SolicitationEntity;
import org.springframework.stereotype.Component;

@Component
public class SolicitationMapper {
    public Solicitation toDomain(SolicitationEntity entity) {
        if (entity == null) return null;
        return Solicitation.builder()
                .id(entity.getId())
                .clientId(entity.getClientId())
                .status(entity.getStatus())
                .currentStep(entity.getCurrentStep())
                .serviceType(entity.getServiceType())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .cep(entity.getCep())
                .street(entity.getStreet())
                .number(entity.getNumber())
                .complement(entity.getComplement())
                .neighborhood(entity.getNeighborhood())
                .city(entity.getCity())
                .state(entity.getState())
                .priority(entity.getPriority())
                .preferredDate(entity.getPreferredDate())
                .estimatedValue(entity.getEstimatedValue())
                .termsAccepted(entity.getTermsAccepted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .submittedAt(entity.getSubmittedAt())
                .analyzedAt(entity.getAnalyzedAt())
                .analyzedBy(entity.getAnalyzedBy())
                .analysisComment(entity.getAnalysisComment())
                .build();
    }

    public SolicitationEntity toEntity(Solicitation domain) {
        if (domain == null) return null;
        return SolicitationEntity.builder()
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