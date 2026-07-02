package com.challenge.backend.application.service;

import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;
import com.challenge.backend.application.port.in.SearchSolicitationsUseCase;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchSolicitationsService implements SearchSolicitationsUseCase {

    private final ElasticsearchPort elasticsearch;
    private final UserRepositoryPort userRepository;

    @Override
    public SearchResult execute(SearchCriteria criteria, Long analystId) {
        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new RuntimeException("Analyst not found"));

        List<String> analystStates = analyst.getCoverageStates() != null ?
                List.copyOf(analyst.getCoverageStates()) : List.of();

        return elasticsearch.search(criteria, analystStates);
    }
}