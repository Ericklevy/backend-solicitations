package com.challenge.backend.application.port.out;

import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;
import com.challenge.backend.domain.model.Solicitation;

import java.util.List;

public interface ElasticsearchPort {
    void indexSolicitation(Solicitation solicitation);
    void deleteSolicitation(Long id);
    SearchResult search(SearchCriteria criteria, List<String> analystStates);
}