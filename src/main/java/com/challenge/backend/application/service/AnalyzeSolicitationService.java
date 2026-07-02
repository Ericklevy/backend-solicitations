package com.challenge.backend.application.service;

import com.challenge.backend.application.port.in.AnalyzeSolicitationUseCase;
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

@Service
@RequiredArgsConstructor
public class AnalyzeSolicitationService implements AnalyzeSolicitationUseCase {

    private final SolicitationRepositoryPort repository;
    private final ElasticsearchPort elasticsearch;
    private final AuditPort audit;

    @Override
    @Transactional
    public Solicitation startAnalysis(Long solicitationId, Long analystId) {
        Solicitation solicitation = repository.findById(solicitationId)
                .orElseThrow(() -> new NotFoundException("Solicitation not found"));

        Result<Void> result = solicitation.startAnalysis(analystId);
        if (result.isFailure()) {
            throw new BusinessException(result.getErrors().get(0).getMessage());
        }

        Solicitation saved = repository.save(solicitation);
        elasticsearch.indexSolicitation(saved);
        audit.log("START_ANALYSIS", analystId.toString(), "ANALYST", saved.getId(), 0, true, null);

        return saved;
    }

    @Override
    @Transactional
    public Solicitation decide(Long solicitationId, Long analystId, boolean approve, String comment) {
        Solicitation solicitation = repository.findById(solicitationId)
                .orElseThrow(() -> new NotFoundException("Solicitation not found"));

        Result<Void> result = solicitation.decide(approve, analystId, comment);
        if (result.isFailure()) {
            throw new BusinessException(result.getErrors().get(0).getMessage());
        }

        Solicitation saved = repository.save(solicitation);
        elasticsearch.indexSolicitation(saved);
        audit.log("DECIDE_SOLICITATION", analystId.toString(), "ANALYST", saved.getId(), 0, true, null);

        return saved;
    }
}