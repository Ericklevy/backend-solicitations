package com.challenge.backend.domain.repository;

import com.challenge.backend.domain.model.Solicitation;
import java.util.List;
import java.util.Optional;

public interface SolicitationRepositoryPort {
    Solicitation save(Solicitation solicitation);
    Optional<Solicitation> findById(Long id);
    List<Solicitation> findByClientId(Long clientId);
    List<Solicitation> findByState(String state);
    void deleteById(Long id);
}