package com.challenge.backend.domain.model;

import com.challenge.backend.domain.model.enums.Role;
import com.challenge.backend.domain.model.enums.Status;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void adminShouldAccessAnySolicitation() {
        User admin = User.builder()
                .id(1L)
                .role(Role.ADMIN)
                .build();

        Solicitation solicitation = Solicitation.builder()
                .clientId(2L)
                .state("SP")
                .build();

        assertTrue(admin.canAccessSolicitation(solicitation));
    }

    @Test
    void clientShouldAccessOwnSolicitation() {
        User client = User.builder()
                .id(2L)
                .role(Role.CLIENT)
                .build();

        Solicitation solicitation = Solicitation.builder()
                .clientId(2L)
                .build();

        assertTrue(client.canAccessSolicitation(solicitation));
    }

    @Test
    void clientShouldNotAccessOthersSolicitation() {
        User client = User.builder()
                .id(2L)
                .role(Role.CLIENT)
                .build();

        Solicitation solicitation = Solicitation.builder()
                .clientId(3L)
                .build();

        assertFalse(client.canAccessSolicitation(solicitation));
    }

    @Test
    void analystWithCoverageShouldAccess() {
        User analyst = User.builder()
                .id(4L)
                .role(Role.ANALYST)
                .coverageStates(Set.of("SP", "RJ"))
                .build();

        Solicitation solicitation = Solicitation.builder()
                .state("SP")
                .build();

        assertTrue(analyst.canAccessSolicitation(solicitation));
    }

    @Test
    void analystWithoutCoverageShouldNotAccess() {
        User analyst = User.builder()
                .id(4L)
                .role(Role.ANALYST)
                .coverageStates(Set.of("SP", "RJ"))
                .build();

        Solicitation solicitation = Solicitation.builder()
                .state("MG")
                .build();

        assertFalse(analyst.canAccessSolicitation(solicitation));
    }

    @Test
    void analystWithNullCoverageStateShouldNotAccess() {
        User analyst = User.builder()
                .id(4L)
                .role(Role.ANALYST)
                .coverageStates(null)
                .build();

        Solicitation solicitation = Solicitation.builder()
                .state("SP")
                .build();

        assertFalse(analyst.canAccessSolicitation(solicitation));
    }

    @Test
    void adminShouldBeAbleToCreateUser() {
        User admin = User.builder().role(Role.ADMIN).build();
        assertTrue(admin.canCreateUser(Role.ANALYST));
    }

    @Test
    void analystShouldNotBeAbleToCreateUser() {
        User analyst = User.builder().role(Role.ANALYST).build();
        assertFalse(analyst.canCreateUser(Role.CLIENT));
    }

    @Test
    void clientShouldNotBeAbleToCreateUser() {
        User client = User.builder().role(Role.CLIENT).build();
        assertFalse(client.canCreateUser(Role.CLIENT));
    }
}
