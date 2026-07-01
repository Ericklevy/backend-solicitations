package com.challenge.backend.domain.model.enums;

public enum Role {
    CLIENT,
    ANALYST,
    ADMIN;

    public boolean isClient() { return this == CLIENT; }
    public boolean isAnalyst() { return this == ANALYST; }
    public boolean isAdmin() { return this == ADMIN; }
    public boolean isInternal() { return this == ANALYST || this == ADMIN; }
}