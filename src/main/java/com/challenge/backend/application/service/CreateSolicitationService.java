package com.challenge.backend.application.service;

import com.challenge.backend.application.port.in.CreateSolicitationUseCase;
import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CreateSolicitationService implements CreateSolicitationUseCase {

    private final SolicitationRepositoryPort repository;
    private final ElasticsearchPort elasticsearch;
    private final AuditPort audit;

    @Override
    @Transactional
    public Solicitation execute(Long clientId) {
        Solicitation solicitation = Solicitation.builder()
                .clientId(clientId)
                .status(Status.DRAFT)
                .currentStep(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Solicitation saved = repository.save(solicitation);
        elasticsearch.indexSolicitation(saved);
        audit.log("CREATE_SOLICITATION", clientId.toString(), "CLIENT", saved.getId(), 0, true, null);

        return saved;
    }
}