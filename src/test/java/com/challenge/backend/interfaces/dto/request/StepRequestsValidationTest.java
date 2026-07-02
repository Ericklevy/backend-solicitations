package com.challenge.backend.interfaces.dto.request;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StepRequestsValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== STEP 1 =====

    @Test
    void step1_shouldPassWithValidData() {
        Step1Request request = new Step1Request();
        request.setServiceType(ServiceType.INSTALLATION);
        request.setTitle("Valid Title");
        request.setDescription("This description is long enough to pass validation (>20 chars)");

        Set<ConstraintViolation<Step1Request>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void step1_shouldFailWithNullServiceType() {
        Step1Request request = new Step1Request();
        request.setServiceType(null);
        request.setTitle("Valid Title");
        request.setDescription("This description is long enough to pass validation (>20 chars)");

        Set<ConstraintViolation<Step1Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("serviceType")));
    }

    @Test
    void step1_shouldFailWithTitleTooShort() {
        Step1Request request = new Step1Request();
        request.setServiceType(ServiceType.INSTALLATION);
        request.setTitle("Ab");
        request.setDescription("This description is long enough to pass validation (>20 chars)");

        Set<ConstraintViolation<Step1Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
    }

    @Test
    void step1_shouldFailWithDescriptionTooShort() {
        Step1Request request = new Step1Request();
        request.setServiceType(ServiceType.INSTALLATION);
        request.setTitle("Valid Title");
        request.setDescription("Short desc");

        Set<ConstraintViolation<Step1Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    // ===== STEP 2 =====

    @Test
    void step2_shouldPassWithValidData() {
        Step2Request request = new Step2Request();
        request.setCep("01001-000");
        request.setNumber("123");

        Set<ConstraintViolation<Step2Request>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void step2_shouldPassWithCepWithoutHyphen() {
        Step2Request request = new Step2Request();
        request.setCep("01001000");
        request.setNumber("123");

        Set<ConstraintViolation<Step2Request>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void step2_shouldFailWithInvalidCepFormat() {
        Step2Request request = new Step2Request();
        request.setCep("1234");
        request.setNumber("123");

        Set<ConstraintViolation<Step2Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("cep")));
    }

    @Test
    void step2_shouldFailWithBlankNumber() {
        Step2Request request = new Step2Request();
        request.setCep("01001-000");
        request.setNumber("");

        Set<ConstraintViolation<Step2Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("number")));
    }

    // ===== STEP 3 =====

    @Test
    void step3_shouldPassWithValidData() {
        Step3Request request = new Step3Request();
        request.setPriority(Priority.MEDIUM);
        request.setPreferredDate(LocalDate.now().plusDays(1));
        request.setEstimatedValue(BigDecimal.valueOf(100));
        request.setTermsAccepted(true);

        Set<ConstraintViolation<Step3Request>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void step3_shouldFailWithNullPriority() {
        Step3Request request = new Step3Request();
        request.setPriority(null);
        request.setPreferredDate(LocalDate.now().plusDays(1));
        request.setEstimatedValue(BigDecimal.valueOf(100));
        request.setTermsAccepted(true);

        Set<ConstraintViolation<Step3Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("priority")));
    }

    @Test
    void step3_shouldFailWithPastDate() {
        Step3Request request = new Step3Request();
        request.setPriority(Priority.MEDIUM);
        request.setPreferredDate(LocalDate.now().minusDays(1));
        request.setEstimatedValue(BigDecimal.valueOf(100));
        request.setTermsAccepted(true);

        Set<ConstraintViolation<Step3Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("preferredDate")));
    }

    @Test
    void step3_shouldFailWithTermsNotAccepted() {
        Step3Request request = new Step3Request();
        request.setPriority(Priority.MEDIUM);
        request.setPreferredDate(LocalDate.now().plusDays(1));
        request.setEstimatedValue(BigDecimal.valueOf(100));
        request.setTermsAccepted(false);

        Set<ConstraintViolation<Step3Request>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("termsAccepted")));
    }
}
