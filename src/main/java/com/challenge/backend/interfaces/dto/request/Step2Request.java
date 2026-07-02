package com.challenge.backend.interfaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Step2Request {
    @NotBlank(message = "CEP is required")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Invalid CEP format")
    private String cep;

    private String street;
    private String neighborhood;
    private String city;
    private String state;

    @NotBlank(message = "Number is required")
    @Size(max = 20, message = "Number must be up to 20 characters")
    private String number;

    private String complement;
}