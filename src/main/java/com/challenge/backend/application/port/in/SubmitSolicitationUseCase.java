package com.challenge.backend.application.port.in;

import com.challenge.backend.domain.model.Solicitation;

public interface SubmitSolicitationUseCase {
    Solicitation execute(Long solicitationId, Long clientId);
}