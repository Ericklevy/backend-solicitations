package com.challenge.backend.application.service;

import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchSolicitationsServiceTest {

    @Mock
    private ElasticsearchPort elasticsearch;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private SearchSolicitationsService service;

    @Test
    void shouldSearchWithAnalystStates() {
        SearchCriteria criteria = SearchCriteria.builder().q("test").build();
        User analyst = User.builder()
                .id(2L)
                .role(Role.ANALYST)
                .coverageStates(Set.of("SP", "RJ"))
                .build();

        SearchResult mockResult = SearchResult.builder().items(Collections.emptyList()).total(0L).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(analyst));
        when(elasticsearch.search(eq(criteria), anyList())).thenReturn(mockResult);

        SearchResult result = service.execute(criteria, 2L);

        assertNotNull(result);
        verify(elasticsearch).search(eq(criteria), argThat(list -> list.contains("SP") && list.contains("RJ") && list.size() == 2));
    }

    @Test
    void shouldSearchWithEmptyCoverageStates() {
        SearchCriteria criteria = SearchCriteria.builder().q("test").build();
        User analyst = User.builder()
                .id(2L)
                .role(Role.ANALYST)
                .coverageStates(null)
                .build();

        SearchResult mockResult = SearchResult.builder().items(Collections.emptyList()).total(0L).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(analyst));
        when(elasticsearch.search(eq(criteria), eq(List.of()))).thenReturn(mockResult);

        SearchResult result = service.execute(criteria, 2L);

        assertNotNull(result);
        verify(elasticsearch).search(criteria, List.of());
    }

    @Test
    void shouldThrowWhenAnalystNotFound() {
        SearchCriteria criteria = SearchCriteria.builder().q("test").build();
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.execute(criteria, 2L));
    }
}
