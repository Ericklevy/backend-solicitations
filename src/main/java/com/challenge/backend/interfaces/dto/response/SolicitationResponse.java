package com.challenge.backend.interfaces.dto.response;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
public class SolicitationResponse {
    private Long id;
    private Long clientId;
    private Status status;
    private int currentStep;

    // Step 1
    private ServiceType serviceType;
    private String title;
    private String description;

    // Step 2
    private String cep;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    // Step 3
    private Priority priority;
    private LocalDate preferredDate;
    private BigDecimal estimatedValue;
    private Boolean termsAccepted;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private Instant submittedAt;
    private Instant analyzedAt;
    private Long analyzedBy;
    private String analysisComment;
}