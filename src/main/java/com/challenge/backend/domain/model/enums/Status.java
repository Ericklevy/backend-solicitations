package com.challenge.backend.domain.model.enums;

public enum Status {
    DRAFT("Rascunho"),
    SUBMITTED("Enviado"),
    IN_REVIEW("Em Análise"),
    APPROVED("Aprovado"),
    REJECTED("Rejeitado");

    private final String description;

    Status(String description) {
        this.description = description;
    }

    public String getDescription() { return description; }

    public boolean canEdit() { return this == DRAFT; }
    public boolean canAnalyze() { return this == SUBMITTED || this == IN_REVIEW; }
    public boolean isTerminal() { return this == APPROVED || this == REJECTED; }
}