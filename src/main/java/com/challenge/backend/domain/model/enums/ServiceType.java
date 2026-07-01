package com.challenge.backend.domain.model.enums;

public enum ServiceType {
    INSTALLATION("Instalação"),
    MAINTENANCE("Manutenção"),
    INSPECTION("Inspeção");

    private final String description;

    ServiceType(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }
}