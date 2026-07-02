package com.challenge.backend.interfaces.exception;

import com.challenge.backend.domain.model.Solicitation.ValidationError;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ValidationError> errors;
}