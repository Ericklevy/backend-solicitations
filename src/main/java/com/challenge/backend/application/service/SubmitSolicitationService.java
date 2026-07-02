package com.challenge.backend.application.service;

import com.challenge.backend.application.port.in.SubmitSolicitationUseCase;
import com.challenge.backend.application.port.out.AuditPort;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.Solicitation.Result;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubmitSolicitationService implements SubmitSolicitationUseCase {

    private final SolicitationRepositoryPort repository;
    private final ElasticsearchPort elasticsearch;
    private final AuditPort audit;

    @Override
    @Transactional
    public Solicitation execute(Long solicitationId, Long clientId) {
        Solicitation solicitation = repository.findById(solicitationId)
                .orElseThrow(() -> new RuntimeException("Solicitation not found"));

        if (!solicitation.getClientId().equals(clientId)) {
            throw new RuntimeException("You can only submit your own solicitations");
        }

        Result<Void> result = solicitation.submit();
        if (result.isFailure()) {
            throw new RuntimeException("Validation failed: " + result.getErrors());
        }

        Solicitation saved = repository.save(solicitation);
        elasticsearch.indexSolicitation(saved);
        audit.log("SUBMIT_SOLICITATION", clientId.toString(), "CLIENT", saved.getId(), 0, true, null);

        return saved;
    }
}