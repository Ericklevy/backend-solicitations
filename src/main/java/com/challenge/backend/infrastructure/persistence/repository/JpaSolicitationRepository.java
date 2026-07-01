package com.challenge.backend.infrastructure.persistence.repository;

import com.challenge.backend.infrastructure.persistence.entity.SolicitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaSolicitationRepository extends JpaRepository<SolicitationEntity, Long> {
    List<SolicitationEntity> findByClientId(Long clientId);

    @Query("SELECT s FROM SolicitationEntity s WHERE s.state IN :states")
    List<SolicitationEntity> findByStates(@Param("states") List<String> states);
}