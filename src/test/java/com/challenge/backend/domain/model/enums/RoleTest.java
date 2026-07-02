package com.challenge.backend.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void clientShouldBeClient() {
        assertTrue(Role.CLIENT.isClient());
        assertFalse(Role.ANALYST.isClient());
    }

    @Test
    void analystShouldBeAnalyst() {
        assertTrue(Role.ANALYST.isAnalyst());
        assertFalse(Role.CLIENT.isAnalyst());
    }

    @Test
    void adminShouldBeAdmin() {
        assertTrue(Role.ADMIN.isAdmin());
        assertFalse(Role.CLIENT.isAdmin());
    }

    @Test
    void analystShouldBeInternal() {
        assertTrue(Role.ANALYST.isInternal());
    }

    @Test
    void adminShouldBeInternal() {
        assertTrue(Role.ADMIN.isInternal());
    }

    @Test
    void clientShouldNotBeInternal() {
        assertFalse(Role.CLIENT.isInternal());
    }
}
