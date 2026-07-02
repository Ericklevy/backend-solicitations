package com.challenge.backend.domain.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PriorityTest {

    @Test
    void lowShouldHaveDescriptionBaixa() {
        assertEquals("Baixa", Priority.LOW.getDescription());
    }

    @Test
    void mediumShouldHaveDescriptionMedia() {
        assertEquals("Média", Priority.MEDIUM.getDescription());
    }

    @Test
    void highShouldHaveDescriptionAlta() {
        assertEquals("Alta", Priority.HIGH.getDescription());
    }
}
