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
import com.challenge.backend.interfaces.exception.BusinessException;
import com.challenge.backend.interfaces.exception.NotFoundException;
import com.challenge.backend.interfaces.exception.UnauthorizedException;

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
                .orElseThrow(() -> new NotFoundException("Solicitation not found"));

        if (!solicitation.getClientId().equals(clientId)) {
            throw new UnauthorizedException("You can only submit your own solicitations");
        }

        Result<Void> result = solicitation.submit();
        if (result.isFailure()) {
            throw new BusinessException("Validation failed: " + result.getErrors());
        }

        Solicitation saved = repository.save(solicitation);
        elasticsearch.indexSolicitation(saved);
        audit.log("SUBMIT_SOLICITATION", clientId.toString(), "CLIENT", saved.getId(), 0, true, null);

        return saved;
    }
}