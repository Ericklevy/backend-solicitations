package com.challenge.backend.interfaces.dto.request;

import com.challenge.backend.domain.model.enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassWithValidData() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Analyst");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setRole(Role.ANALYST);
        request.setCoverageStates(Set.of("SP", "RJ"));

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailWithBlankName() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setRole(Role.ANALYST);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void shouldFailWithBlankEmail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Analyst");
        request.setEmail("");
        request.setPassword("password123");
        request.setRole(Role.ANALYST);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWithInvalidEmail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Analyst");
        request.setEmail("invalidemail");
        request.setPassword("password123");
        request.setRole(Role.ANALYST);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shouldFailWithNullRole() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Analyst");
        request.setEmail("jane@example.com");
        request.setPassword("password123");
        request.setRole(null);

        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("role")));
    }
}
