package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.in.CreateSolicitationUseCase;
import com.challenge.backend.application.port.in.SubmitSolicitationUseCase;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.service.SolicitationDomainService;
import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.interfaces.dto.request.Step1Request;
import com.challenge.backend.interfaces.dto.request.Step2Request;
import com.challenge.backend.interfaces.dto.request.Step3Request;
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

@RestController
@RequestMapping("/api/solicitations")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Solicitations", description = "Client solicitation endpoints")
public class SolicitationController {

    private final CreateSolicitationUseCase createSolicitationUseCase;
    private final SubmitSolicitationUseCase submitSolicitationUseCase;
    private final SolicitationDomainService domainService;
    private final SolicitationResponseMapper responseMapper;

    @PostMapping
    @Operation(summary = "Create a new solicitation draft")
    public ResponseEntity<SolicitationResponse> create(Authentication authentication) {
        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = createSolicitationUseCase.execute(clientId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get solicitation by ID")
    public ResponseEntity<SolicitationResponse> getById(@PathVariable Long id, Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(userId)) {
            throw new RuntimeException("You can only view your own solicitations");
        }

        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @PatchMapping("/{id}/step1")
    @Operation(summary = "Save Step 1 of solicitation")
    public ResponseEntity<SolicitationResponse> saveStep1(
            @PathVariable Long id,
            @Valid @RequestBody Step1Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new RuntimeException("You can only edit your own solicitations");
        }

        Solicitation.Result<Void> result = solicitation.saveStep1(
                request.getServiceType(),
                request.getTitle(),
                request.getDescription()
        );

        if (result.isFailure()) {
            throw new RuntimeException("Validation failed: " + result.getErrors());
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/step2")
    @Operation(summary = "Save Step 2 of solicitation with CEP validation")
    public ResponseEntity<SolicitationResponse> saveStep2(
            @PathVariable Long id,
            @Valid @RequestBody Step2Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new RuntimeException("You can only edit your own solicitations");
        }

        Solicitation.Result<Void> result = solicitation.saveStep2(
                request.getCep(),
                request.getStreet(),
                request.getNumber(),
                request.getComplement(),
                request.getNeighborhood(),
                request.getCity(),
                request.getState()
        );

        if (result.isFailure()) {
            throw new RuntimeException("Validation failed: " + result.getErrors());
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/step3")
    @Operation(summary = "Save Step 3 of solicitation")
    public ResponseEntity<SolicitationResponse> saveStep3(
            @PathVariable Long id,
            @Valid @RequestBody Step3Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new RuntimeException("You can only edit your own solicitations");
        }

        Solicitation.Result<Void> result = solicitation.saveStep3(
                request.getPriority(),
                request.getPreferredDate(),
                request.getEstimatedValue(),
                request.isTermsAccepted()
        );

        if (result.isFailure()) {
            throw new RuntimeException("Validation failed: " + result.getErrors());
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PostMapping("/{id}/submit")
    @Auditable(action = "SUBMIT_SOLICITATION")
    @Operation(summary = "Submit solicitation for analysis")
    public ResponseEntity<SolicitationResponse> submit(
            @PathVariable Long id,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = submitSolicitationUseCase.execute(id, clientId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }
}