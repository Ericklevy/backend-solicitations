package com.challenge.backend.interfaces.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AuthRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWithValidData() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWithBlankEmail() {
        AuthRequest request = new AuthRequest();
        request.setEmail("");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWithInvalidEmail() {
        AuthRequest request = new AuthRequest();
        request.setEmail("invalidemail");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWithBlankPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void shouldFailWithPasswordTooShort() {
        AuthRequest request = new AuthRequest();
        request.setEmail("user@example.com");
        request.setPassword("12345");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
