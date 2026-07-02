package com.challenge.backend.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceTypeTest {

    @Test
    void installationShouldHaveCorrectDescription() {
        assertEquals("Instalação", ServiceType.INSTALLATION.getDescription());
    }

    @Test
    void maintenanceShouldHaveCorrectDescription() {
        assertEquals("Manutenção", ServiceType.MAINTENANCE.getDescription());
    }

    @Test
    void inspectionShouldHaveCorrectDescription() {
        assertEquals("Inspeção", ServiceType.INSPECTION.getDescription());
    }
}
