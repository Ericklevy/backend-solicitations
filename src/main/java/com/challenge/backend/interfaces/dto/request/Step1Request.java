package com.challenge.backend.interfaces.dto.request;

import com.challenge.backend.domain.model.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Step1Request {
    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 80, message = "Title must be between 3 and 80 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 20, max = 1000, message = "Description must be between 20 and 1000 characters")
    private String description;
}