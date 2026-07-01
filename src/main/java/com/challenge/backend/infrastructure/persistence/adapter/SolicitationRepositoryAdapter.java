package com.challenge.backend.infrastructure.persistence.adapter;

import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.repository.SolicitationRepositoryPort;
import com.challenge.backend.infrastructure.persistence.entity.SolicitationEntity;
import com.challenge.backend.infrastructure.persistence.mapper.SolicitationMapper;
import com.challenge.backend.infrastructure.persistence.repository.JpaSolicitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SolicitationRepositoryAdapter implements SolicitationRepositoryPort {
    private final JpaSolicitationRepository jpaRepository;
    private final SolicitationMapper mapper;

    @Override
    public Solicitation save(Solicitation solicitation) {
        SolicitationEntity entity = mapper.toEntity(solicitation);
        SolicitationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Solicitation> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Solicitation> findByClientId(Long clientId) {
        return jpaRepository.findByClientId(clientId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Solicitation> findByState(String state) {
        return jpaRepository.findByStates(List.of(state))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}