package com.challenge.backend.interfaces.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DecideRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWithValidData() {
        DecideRequest request = new DecideRequest();
        request.setDecision("APPROVE");
        request.setComment("This is a valid comment with more than 10 characters");

        Set<ConstraintViolation<DecideRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWithBlankDecision() {
        DecideRequest request = new DecideRequest();
        request.setDecision("");
        request.setComment("This is a valid comment with more than 10 characters");

        Set<ConstraintViolation<DecideRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("decision")));
    }

    @Test
    void shouldFailWithBlankComment() {
        DecideRequest request = new DecideRequest();
        request.setDecision("APPROVE");
        request.setComment("");

        Set<ConstraintViolation<DecideRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("comment")));
    }

    @Test
    void shouldFailWithCommentTooShort() {
        DecideRequest request = new DecideRequest();
        request.setDecision("APPROVE");
        request.setComment("Short");

        Set<ConstraintViolation<DecideRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("comment")));
    }
}
