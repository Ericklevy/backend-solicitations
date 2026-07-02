package com.challenge.backend.application.port.in;

import com.challenge.backend.domain.model.Solicitation;

public interface AnalyzeSolicitationUseCase {
    Solicitation startAnalysis(Long solicitationId, Long analystId);
    Solicitation decide(Long solicitationId, Long analystId, boolean approve, String comment);
}