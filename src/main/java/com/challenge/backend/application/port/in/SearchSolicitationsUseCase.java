package com.challenge.backend.application.port.in;

import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;

public interface SearchSolicitationsUseCase {
    SearchResult execute(SearchCriteria criteria, Long analystId);
}