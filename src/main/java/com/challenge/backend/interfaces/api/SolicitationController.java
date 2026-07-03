package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.dto.AddressInfo;
import com.challenge.backend.application.port.in.CreateSolicitationUseCase;
import com.challenge.backend.application.port.in.SubmitSolicitationUseCase;
import com.challenge.backend.application.port.out.CepServicePort;
import com.challenge.backend.domain.model.Solicitation;
import com.challenge.backend.domain.service.SolicitationDomainService;
import com.challenge.backend.infrastructure.audit.annotation.Auditable;
import com.challenge.backend.interfaces.dto.request.Step1Request;
import com.challenge.backend.interfaces.dto.request.Step2Request;
import com.challenge.backend.interfaces.dto.request.Step3Request;
import com.challenge.backend.interfaces.dto.response.SolicitationResponse;
import com.challenge.backend.interfaces.exception.BusinessException;
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

import java.util.stream.Collectors;

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
    private final CepServicePort cepServicePort;

    @PostMapping
    @Operation(
        summary = "01 - Create a new solicitation draft",
        description = "Creates a new empty solicitation in DRAFT status. Returns the solicitation ID that will be used in all the steps below.",
        operationId = "01-createSolicitation"
    )
    public ResponseEntity<SolicitationResponse> create(Authentication authentication) {
        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = createSolicitationUseCase.execute(clientId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @PatchMapping("/{id}/step1")
    @Operation(
        summary = "02 - Save Step 1: Service type and description",
        description = "Step 1 of 3. Fill in the service type, title and description. The {id} is the solicitation ID returned by the 'Create' endpoint above.",
        operationId = "02-saveStep1"
    )
    public ResponseEntity<SolicitationResponse> saveStep1(
            @Parameter(description = "ID of the solicitation created in step '01 - Create'. Example: 1") @PathVariable Long id,
            @Valid @RequestBody Step1Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new BusinessException("You can only edit your own solicitations");
        }

        Solicitation.Result<Void> result = solicitation.saveStep1(
                request.getServiceType(),
                request.getTitle(),
                request.getDescription()
        );

        if (result.isFailure()) {
            String msg = result.getErrors().stream()
                    .map(e -> e.getField() + ": " + e.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BusinessException(msg);
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/step2")
    @Operation(
        summary = "03 - Save Step 2: Address with automatic CEP lookup",
        description = "Step 2 of 3. Fill in the CEP and the system will automatically look up the address using the ViaCEP API. The {id} is the solicitation ID returned by the 'Create' endpoint.",
        operationId = "03-saveStep2"
    )
    public ResponseEntity<SolicitationResponse> saveStep2(
            @Parameter(description = "ID of the solicitation created in step '01 - Create'. Example: 1") @PathVariable Long id,
            @Valid @RequestBody Step2Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new BusinessException("You can only edit your own solicitations");
        }

        // Busca o endereço completo via ViaCEP e sobrescreve os campos
        AddressInfo address = cepServicePort.getAddressByCep(request.getCep());
        String street       = (address != null && address.getStreet()       != null) ? address.getStreet()       : request.getStreet();
        String neighborhood = (address != null && address.getNeighborhood() != null) ? address.getNeighborhood() : request.getNeighborhood();
        String city         = (address != null && address.getCity()         != null) ? address.getCity()         : request.getCity();
        String state        = (address != null && address.getState()        != null) ? address.getState()        : request.getState();

        Solicitation.Result<Void> result = solicitation.saveStep2(
                request.getCep(),
                street,
                request.getNumber(),
                request.getComplement(),
                neighborhood,
                city,
                state
        );

        if (result.isFailure()) {
            String msg = result.getErrors().stream()
                    .map(e -> e.getField() + ": " + e.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BusinessException(msg);
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PatchMapping("/{id}/step3")
    @Operation(
        summary = "04 - Save Step 3: Priority, preferred date and value",
        description = "Step 3 of 3. Fill in priority (LOW/MEDIUM/HIGH), preferred date, estimated value and accept the terms. The {id} is the solicitation ID returned by the 'Create' endpoint.",
        operationId = "04-saveStep3"
    )
    public ResponseEntity<SolicitationResponse> saveStep3(
            @Parameter(description = "ID of the solicitation created in step '01 - Create'. Example: 1") @PathVariable Long id,
            @Valid @RequestBody Step3Request request,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(clientId)) {
            throw new BusinessException("You can only edit your own solicitations");
        }

        Solicitation.Result<Void> result = solicitation.saveStep3(
                request.getPriority(),
                request.getPreferredDate(),
                request.getEstimatedValue(),
                request.isTermsAccepted()
        );

        if (result.isFailure()) {
            String msg = result.getErrors().stream()
                    .map(e -> e.getField() + ": " + e.getMessage())
                    .collect(Collectors.joining("; "));
            throw new BusinessException(msg);
        }

        Solicitation saved = domainService.save(solicitation);
        return ResponseEntity.ok(responseMapper.toResponse(saved));
    }

    @PostMapping("/{id}/submit")
    @Auditable(action = "SUBMIT_SOLICITATION")
    @Operation(
        summary = "05 - Submit solicitation for analysis",
        description = "Submits the solicitation after all 3 steps are completed. Changes status from DRAFT to SUBMITTED and indexes it in Elasticsearch so analysts can find it. The {id} is the solicitation ID returned by the 'Create' endpoint.",
        operationId = "05-submitSolicitation"
    )
    public ResponseEntity<SolicitationResponse> submit(
            @Parameter(description = "ID of the solicitation created in step '01 - Create'. Example: 1") @PathVariable Long id,
            Authentication authentication) {

        Long clientId = Long.parseLong(authentication.getName());
        Solicitation solicitation = submitSolicitationUseCase.execute(id, clientId);
        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "06 - Get solicitation details by ID",
        description = "Returns all data of a solicitation. The {id} is the solicitation ID returned by the 'Create' endpoint. Only the client who owns the solicitation can access it.",
        operationId = "06-getSolicitation"
    )
    public ResponseEntity<SolicitationResponse> getById(
            @Parameter(description = "ID of the solicitation created in step '01 - Create'. Example: 1") @PathVariable Long id,
            Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        Solicitation solicitation = domainService.findById(id);

        if (!solicitation.getClientId().equals(userId)) {
            throw new RuntimeException("You can only view your own solicitations");
        }

        return ResponseEntity.ok(responseMapper.toResponse(solicitation));
    }

    @GetMapping
    @Operation(
        summary = "07 - Get all solicitations for the authenticated client",
        description = "Returns a list of all solicitations belonging to the authenticated client.",
        operationId = "07-getClientSolicitations"
    )
    public ResponseEntity<java.util.List<SolicitationResponse>> getMySolicitations(Authentication authentication) {
        Long clientId = Long.parseLong(authentication.getName());
        java.util.List<Solicitation> solicitations = domainService.findByClientId(clientId);
        java.util.List<SolicitationResponse> response = solicitations.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}