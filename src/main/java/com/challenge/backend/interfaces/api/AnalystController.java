package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.dto.SearchCriteria;
import com.challenge.backend.application.dto.SearchResult;
import com.challenge.backend.application.port.in.AnalyzeSolicitationUseCase;
import com.challenge.backend.application.port.in.SearchSolicitationsUseCase;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.model.User;
import com.challenge.backend.domain.repository.UserRepositoryPort;
import com.challenge.backend.domain.service.SolicitationDomainService;
import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.interfaces.dto.request.DecideRequest;
import com.challenge.backend.interfaces.dto.response.PageResponse;
import com.challenge.backend.interfaces.dto.response.SolicitationResponse;
import com.challenge.backend.interfaces.mapper.SolicitationResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/analyst/solicitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Analyst", description = "Analyst endpoints for solicitation management")
public class AnalystController {

    private final SolicitationDomainService domainService;
    private final AnalyzeSolicitationUseCase analyzeSolicitationUseCase;
    private final SearchSolicitationsUseCase searchSolicitationsUseCase;
    private final UserRepositoryPort userRepository;
    private final SolicitationResponseMapper responseMapper;

    @GetMapping("/search")
    @Operation(
        summary = "01 - Search solicitations with filters (Elasticsearch)",
        description = "Searches solicitations using Elasticsearch. The analyst only sees solicitations from states they have coverage for. " +
                "Available filters: q (text search), status (DRAFT/SUBMITTED/IN_REVIEW/APPROVED/REJECTED), serviceType, priority, state (UF), dateFrom, dateTo. " +
                "NOTE: This endpoint does NOT require a path {id}. Leave the {id} fields blank.",
        operationId = "analyst-01-search"
    )
    public ResponseEntity<PageResponse<SolicitationResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "submittedAt") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());

        SearchCriteria criteria = SearchCriteria.builder()
                .q(q)
                .status(status)
                .serviceType(serviceType)
                .priority(priority)
                .state(state)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .page(page)
                .size(size)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();

        SearchResult result = searchSolicitationsUseCase.execute(criteria, analystId);

        List<SolicitationResponse> responses = result.getItems().stream()
                .map(responseMapper::toResponse)
                .toList();

        PageResponse<SolicitationResponse> response = PageResponse.<SolicitationResponse>builder()
                .items(responses)
                .page(result.getPage())
                .size(result.getSize())
                .total(result.getTotal())
                .totalPages((int) Math.ceil((double) result.getTotal() / result.getSize()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "02 - Get solicitation by ID (analyst view)",
        description = "Returns the full details of a solicitation. The {id} is the SOLICITATION ID (the numeric ID returned by the search endpoint or the populate endpoint). " +
                "The analyst can only view solicitations from states they have coverage for.",
        operationId = "analyst-02-getById"
    )
    public ResponseEntity<SolicitationResponse> getById(
            @Parameter(description = "ID of the SOLICITATION (not the analyst). Example: if the search returned a solicitation with 'id: 1', use 1 here.") @PathVariable Long id,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());
        User analyst = userRepository.findById(analystId)
                .orElseThrow(() -> new RuntimeException("Analyst not found"));

        Solicitation solicitation = domainService.findById(id);

        if (analyst.getCoverageStates() != null &&
                !analyst.getCoverageStates().contains(solicitation.getState())) {
            throw new RuntimeException("You don't have coverage for this solicitation's state");
        }

        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @PostMapping("/{id}/start")
    @Auditable(action = "START_ANALYSIS")
    @Operation(
        summary = "03 - Start analysis of a solicitation",
        description = "Changes the solicitation status from SUBMITTED to IN_REVIEW, assigning it to the logged-in analyst. " +
                "The {id} is the SOLICITATION ID (the numeric ID from the search results). " +
                "Example: if the search returned 'id: 5', use 5 here.",
        operationId = "analyst-03-startAnalysis"
    )
    public ResponseEntity<SolicitationResponse> startAnalysis(
            @Parameter(description = "ID of the SOLICITATION to start reviewing. Example: 5") @PathVariable Long id,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());
        Solicitation solicitation = analyzeSolicitationUseCase.startAnalysis(id, analystId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @PostMapping("/{id}/decide")
    @Auditable(action = "DECIDE_SOLICITATION")
    @Operation(
        summary = "04 - Decide on a solicitation (APPROVE/REJECT)",
        description = "Final step: approves or rejects a solicitation that is IN_REVIEW. " +
                "The {id} is the SOLICITATION ID (same ID used in the 'start' endpoint). " +
                "Send body: { \"decision\": \"APPROVE\", \"comment\": \"Looks good!\" } or { \"decision\": \"REJECT\", \"comment\": \"Reason here\" }",
        operationId = "analyst-04-decide"
    )
    public ResponseEntity<SolicitationResponse> decide(
            @Parameter(description = "ID of the SOLICITATION to approve or reject. Must be IN_REVIEW status. Example: 5") @PathVariable Long id,
            @Valid @RequestBody DecideRequest request,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());
        boolean approve = "APPROVE".equalsIgnoreCase(request.getDecision());
        Solicitation solicitation = analyzeSolicitationUseCase.decide(id, analystId, approve, request.getComment());
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }
}