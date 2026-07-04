package com.challenge.backend.interfaces.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class CoverageUpdateRequest {
    private Set<String> coverageStates;
    private Set<String> states;

    public Set<String> getEffectiveStates() {
        if (coverageStates != null && !coverageStates.isEmpty()) {
            return coverageStates;
        }
        if (states != null && !states.isEmpty()) {
            return states;
        }
        return Set.of();
    }
}
