package com.challenge.backend.infrastructure.search.adapter;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.json.JsonData;
import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;
import com.challenge.backend.application.port.out.ElasticsearchPort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import com.challenge.backend.infrastructure.search.document.SolicitationDocument;
import com.challenge.backend.infrastructure.search.repository.SolicitationElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchAdapter implements ElasticsearchPort {

    private final SolicitationElasticsearchRepository documentRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void indexSolicitation(Solicitation solicitation) {
        try {
            SolicitationDocument doc = toDocument(solicitation);
            documentRepository.save(doc);
            log.debug("Solicitation indexed in Elasticsearch: {}", solicitation.getId());
        } catch (Exception e) {
            log.error("Failed to index solicitation in Elasticsearch: {}", solicitation.getId(), e);
        }
    }

    @Override
    public void deleteSolicitation(Long id) {
        try {
            documentRepository.deleteById(id.toString());
            log.debug("Solicitation deleted from Elasticsearch: {}", id);
        } catch (Exception e) {
            log.error("Failed to delete solicitation from Elasticsearch: {}", id, e);
        }
    }

    @Override
    public SearchResult search(SearchCriteria criteria, List<String> analystStates) {
        try {
            NativeQueryBuilder queryBuilder = new NativeQueryBuilder();
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // 1. Busca por termo (q) nos campos title e description
            if (criteria.getQ() != null && !criteria.getQ().trim().isEmpty()) {
                String q = criteria.getQ().trim();
                boolQuery.must(m -> m.multiMatch(mm -> mm
                        .fields("title", "description")
                        .query(q)
                ));
            }

            // 2. Filtro de Status
            if (criteria.getStatus() != null && !criteria.getStatus().isEmpty()) {
                List<FieldValue> statusValues = criteria.getStatus().stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList());
                boolQuery.filter(f -> f.terms(t -> t
                        .field("status")
                        .terms(ts -> ts.value(statusValues))
                ));
            }

            // 3. Filtro de ServiceType
            if (criteria.getServiceType() != null && !criteria.getServiceType().trim().isEmpty()) {
                String serviceType = criteria.getServiceType().trim();
                boolQuery.filter(f -> f.term(t -> t
                        .field("serviceType")
                        .value(serviceType)
                ));
            }

            // 4. Filtro de Priority
            if (criteria.getPriority() != null && !criteria.getPriority().trim().isEmpty()) {
                String priority = criteria.getPriority().trim();
                boolQuery.filter(f -> f.term(t -> t
                        .field("priority")
                        .value(priority)
                ));
            }

            // 5. Filtro de Cobertura de UF do Analista vs Estado solicitado
            if (analystStates != null && !analystStates.isEmpty()) {
                List<FieldValue> stateValues = analystStates.stream()
                        .map(FieldValue::of)
                        .collect(Collectors.toList());

                if (criteria.getState() != null && !criteria.getState().trim().isEmpty()) {
                    String requestedState = criteria.getState().trim().toUpperCase();
                    if (analystStates.contains(requestedState)) {
                        boolQuery.filter(f -> f.term(t -> t.field("state").value(requestedState)));
                    } else {
                        boolQuery.filter(f -> f.terms(t -> t
                                .field("state")
                                .terms(ts -> ts.value(stateValues))
                        ));
                    }
                } else {
                    boolQuery.filter(f -> f.terms(t -> t
                            .field("state")
                            .terms(ts -> ts.value(stateValues))
                    ));
                }
            } else if (criteria.getState() != null && !criteria.getState().trim().isEmpty()) {
                String state = criteria.getState().trim().toUpperCase();
                boolQuery.filter(f -> f.term(t -> t.field("state").value(state)));
            }

            // 6. Filtro de Data (dateFrom / dateTo) sobre submittedAt
            if (criteria.getDateFrom() != null || criteria.getDateTo() != null) {
                boolQuery.filter(f -> f.range(r -> {
                    r.field("submittedAt");
                    if (criteria.getDateFrom() != null) {
                        r.gte(JsonData.of(criteria.getDateFrom().toString()));
                    }
                    if (criteria.getDateTo() != null) {
                        r.lte(JsonData.of(criteria.getDateTo().toString()));
                    }
                    return r;
                }));
            }

            // Construir a query
            queryBuilder.withQuery(q -> q.bool(boolQuery.build()));

            // Paginação e ordenação
            int page = Math.max(0, criteria.getPage());
            int size = criteria.getSize() <= 0 ? 10 : criteria.getSize();

            Sort.Direction direction = "asc".equalsIgnoreCase(criteria.getSortDirection())
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            String sortField = criteria.getSortField() != null && !criteria.getSortField().trim().isEmpty()
                    ? criteria.getSortField().trim() : "submittedAt";

            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
            queryBuilder.withPageable(pageable);

            NativeQuery nativeQuery = queryBuilder.build();
            SearchHits<SolicitationDocument> hits = elasticsearchOperations.search(nativeQuery, SolicitationDocument.class);

            List<Solicitation> items = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::toDomain)
                    .collect(Collectors.toList());

            return SearchResult.builder()
                    .items(items)
                    .page(page)
                    .size(size)
                    .total(hits.getTotalHits())
                    .build();

        } catch (Exception e) {
            log.error("Error searching solicitations", e);
            return SearchResult.builder()
                    .items(List.of())
                    .page(criteria.getPage())
                    .size(criteria.getSize())
                    .total(0L)
                    .build();
        }
    }

    private SolicitationDocument toDocument(Solicitation s) {
        if (s == null) return null;
        return SolicitationDocument.builder()
                .id(s.getId() != null ? s.getId().toString() : null)
                .solicitationId(s.getId())
                .clientId(s.getClientId())
                .status(s.getStatus() != null ? s.getStatus().name() : null)
                .serviceType(s.getServiceType() != null ? s.getServiceType().name() : null)
                .title(s.getTitle())
                .description(s.getDescription())
                .state(s.getState())
                .city(s.getCity())
                .priority(s.getPriority() != null ? s.getPriority().name() : null)
                .createdAt(s.getCreatedAt())
                .submittedAt(s.getSubmittedAt())
                .build();
    }

    private Solicitation toDomain(SolicitationDocument doc) {
        if (doc == null) return null;
        return Solicitation.builder()
                .id(doc.getSolicitationId())
                .clientId(doc.getClientId())
                .status(doc.getStatus() != null ? Status.valueOf(doc.getStatus()) : null)
                .serviceType(doc.getServiceType() != null ? ServiceType.valueOf(doc.getServiceType()) : null)
                .title(doc.getTitle())
                .description(doc.getDescription())
                .state(doc.getState())
                .city(doc.getCity())
                .priority(doc.getPriority() != null ? Priority.valueOf(doc.getPriority()) : null)
                .createdAt(doc.getCreatedAt())
                .submittedAt(doc.getSubmittedAt())
                .build();
    }
}