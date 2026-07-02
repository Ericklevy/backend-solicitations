package com.challenge.backend.interfaces.dto.request;

import com.challenge.backend.domain.model.enums.Priority;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class Step3Request {
    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Preferred date is required")
    @FutureOrPresent(message = "Preferred date cannot be in the past")
    private LocalDate preferredDate;

    @NotNull(message = "Estimated value is required")
    @DecimalMin(value = "0.0", message = "Estimated value must be >= 0")
    private BigDecimal estimatedValue;

    @AssertTrue(message = "Terms must be accepted")
    private boolean termsAccepted;
}