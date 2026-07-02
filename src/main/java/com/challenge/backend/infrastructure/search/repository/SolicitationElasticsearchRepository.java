package com.challenge.backend.infrastructure.search.repository;

import com.challenge.backend.infrastructure.search.document.SolicitationDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitationElasticsearchRepository
        extends ElasticsearchRepository<SolicitationDocument, String> {
}