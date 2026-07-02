package com.challenge.backend.application.port.in;

import com.challenge.backend.domain.model.Solicitation;

public interface CreateSolicitationUseCase {
    Solicitation execute(Long clientId);
}