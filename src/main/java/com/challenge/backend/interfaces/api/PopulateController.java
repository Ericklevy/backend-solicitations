package com.challenge.backend.interfaces.api;

import com.challenge.backend.application.port.in.PopulateSystemUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/populate")
@RequiredArgsConstructor
@Profile("!prod")
@Tag(name = "Populate", description = "Populate system with sample data (dev only)")
public class PopulateController {

    private final PopulateSystemUseCase populateSystemUseCase;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Populate system with sample data (DEV only)")
    public ResponseEntity<Map<String, Object>> populate() {
        Map<String, Object> response = populateSystemUseCase.execute();
        
        if (Boolean.TRUE.equals(response.get("success"))) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
