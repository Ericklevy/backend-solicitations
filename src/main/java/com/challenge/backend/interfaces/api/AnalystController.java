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

    @GetMapping("/{id}")
    @Operation(summary = "Get solicitation by ID (analyst view)")
    public ResponseEntity<SolicitationResponse> getById(
            @PathVariable Long id,
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
    @Operation(summary = "Start analysis of a solicitation")
    public ResponseEntity<SolicitationResponse> startAnalysis(
            @PathVariable Long id,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());
        Solicitation solicitation = analyzeSolicitationUseCase.startAnalysis(id, analystId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @PostMapping("/{id}/decide")
    @Auditable(action = "DECIDE_SOLICITATION")
    @Operation(summary = "Decide on a solicitation (APPROVE/REJECT)")
    public ResponseEntity<SolicitationResponse> decide(
            @PathVariable Long id,
            @Valid @RequestBody DecideRequest request,
            Authentication authentication) {

        Long analystId = Long.parseLong(authentication.getName());
        boolean approve = "APPROVE".equalsIgnoreCase(request.getDecision());
        Solicitation solicitation = analyzeSolicitationUseCase.decide(id, analystId, approve, request.getComment());
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @GetMapping("/search")
    @Operation(summary = "Search solicitations with filters")
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
}