package com.challenge.backend.domain.service;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitationDomainService {
    private final SolicitationRepositoryPort repository;

    @Transactional(readOnly = true)
    public Solicitation findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitation not found with id: " + id));
    }

    @Transactional
    public Solicitation save(Solicitation solicitation) {
        return repository.save(solicitation);
    }

    @Transactional(readOnly = true)
    public List<Solicitation> findByClientId(Long clientId) {
        return repository.findByClientId(clientId);
    }

    @Transactional(readOnly = true)
    public List<Solicitation> findByState(String state) {
        return repository.findByState(state);
    }

    @Transactional(readOnly = true)
    public boolean canEdit(Long solicitationId, Long userId) {
        Solicitation solicitation = findById(solicitationId);
        return solicitation.getStatus().canEdit() &&
                solicitation.getClientId().equals(userId);
    }
}