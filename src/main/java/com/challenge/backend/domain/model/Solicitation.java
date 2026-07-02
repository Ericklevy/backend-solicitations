package com.challenge.backend.domain.model;

import com.challenge.backend.domain.model.enums.Priority;
import com.challenge.backend.domain.model.enums.ServiceType;
import com.challenge.backend.domain.model.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class Solicitation {
    private Long id;
    private Long clientId;
    private Status status;
    private int currentStep;

    // Step 1
    private ServiceType serviceType;
    private String title;
    private String description;

    // Step 2
    private String cep;
    private String street;
    private String number;
    private String complement;
    private String neighborhood;
    private String city;
    private String state;

    // Step 3
    private Priority priority;
    private LocalDate preferredDate;
    private BigDecimal estimatedValue;
    private Boolean termsAccepted;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private Instant submittedAt;
    private Instant analyzedAt;
    private Long analyzedBy;
    private String analysisComment;

    // ===== MÉTODOS DE NEGÓCIO =====

    public Result<Void> saveStep1(ServiceType serviceType, String title, String description) {
        if (!status.canEdit()) {
            return Result.fail("Cannot edit solicitation with status: " + status);
        }

        List<ValidationError> errors = new ArrayList<>();

        if (serviceType == null) {
            errors.add(ValidationError.of("serviceType", "Service type is required"));
        }
        if (title == null || title.trim().length() < 3 || title.trim().length() > 80) {
            errors.add(ValidationError.of("title", "Title must be between 3 and 80 characters"));
        }
        if (description == null || description.trim().length() < 20 || description.trim().length() > 1000) {
            errors.add(ValidationError.of("description", "Description must be between 20 and 1000 characters"));
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        this.serviceType = serviceType;
        this.title = title.trim();
        this.description = description.trim();
        this.currentStep = Math.max(this.currentStep, 1);
        this.updatedAt = Instant.now();

        return Result.ok();
    }

    public Result<Void> saveStep2(String cep, String street, String number, String complement,
                                  String neighborhood, String city, String state) {
        if (!status.canEdit()) {
            return Result.fail("Cannot edit solicitation with status: " + status);
        }

        List<ValidationError> errors = new ArrayList<>();

        if (cep == null || cep.trim().isEmpty()) {
            errors.add(ValidationError.of("cep", "CEP is required"));
        } else {
            String cleanCep = cep.replaceAll("\\D", "");
            if (cleanCep.length() != 8) {
                errors.add(ValidationError.of("cep", "Invalid CEP format"));
            }
        }

        if (number == null || number.trim().isEmpty() || number.length() > 20) {
            errors.add(ValidationError.of("number", "Number is required and must be up to 20 characters"));
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        this.cep = cep.replaceAll("\\D", "");
        this.street = street;
        this.number = number;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.city = city;
        this.state = state;
        this.updatedAt = Instant.now();

        if (isStep2Complete()) {
            this.currentStep = Math.max(this.currentStep, 2);
        }

        return Result.ok();
    }

    public Result<Void> saveStep3(Priority priority, LocalDate preferredDate,
                                  BigDecimal estimatedValue, boolean termsAccepted) {
        if (!status.canEdit()) {
            return Result.fail("Cannot edit solicitation with status: " + status);
        }

        List<ValidationError> errors = new ArrayList<>();

        if (priority == null) {
            errors.add(ValidationError.of("priority", "Priority is required"));
        }
        if (preferredDate == null) {
            errors.add(ValidationError.of("preferredDate", "Preferred date is required"));
        } else if (preferredDate.isBefore(LocalDate.now())) {
            errors.add(ValidationError.of("preferredDate", "Preferred date cannot be in the past"));
        }
        if (estimatedValue == null || estimatedValue.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(ValidationError.of("estimatedValue", "Estimated value must be >= 0"));
        }
        if (!termsAccepted) {
            errors.add(ValidationError.of("termsAccepted", "Terms must be accepted"));
        }
        if (priority == Priority.HIGH && estimatedValue != null &&
                estimatedValue.compareTo(BigDecimal.valueOf(100)) < 0) {
            errors.add(ValidationError.of("estimatedValue", "HIGH priority requires estimated value >= 100"));
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        this.priority = priority;
        this.preferredDate = preferredDate;
        this.estimatedValue = estimatedValue;
        this.termsAccepted = termsAccepted;
        this.currentStep = 3;
        this.updatedAt = Instant.now();

        return Result.ok();
    }

    public Result<Void> submit() {
        if (this.status != Status.DRAFT) {
            return Result.fail("Solicitation must be in DRAFT status to submit");
        }

        List<ValidationError> errors = new ArrayList<>();

        // Step 1
        if (serviceType == null) {
            errors.add(ValidationError.of("step1", "Step 1 incomplete: serviceType is required"));
        }
        if (title == null || title.trim().isEmpty()) {
            errors.add(ValidationError.of("step1", "Step 1 incomplete: title is required"));
        }
        if (description == null || description.trim().isEmpty()) {
            errors.add(ValidationError.of("step1", "Step 1 incomplete: description is required"));
        }

        // Step 2
        if (!isStep2Complete()) {
            errors.add(ValidationError.of("step2", "Step 2 incomplete: address is required"));
        }

        // Step 3
        if (priority == null) {
            errors.add(ValidationError.of("step3", "Step 3 incomplete: priority is required"));
        }
        if (preferredDate == null || preferredDate.isBefore(LocalDate.now())) {
            errors.add(ValidationError.of("step3", "Step 3 incomplete: valid preferred date is required"));
        }
        if (estimatedValue == null || estimatedValue.compareTo(BigDecimal.ZERO) < 0) {
            errors.add(ValidationError.of("step3", "Step 3 incomplete: estimated value is required"));
        }
        if (!Boolean.TRUE.equals(termsAccepted)) {
            errors.add(ValidationError.of("step3", "Step 3 incomplete: terms must be accepted"));
        }
        if (priority == Priority.HIGH && estimatedValue != null &&
                estimatedValue.compareTo(BigDecimal.valueOf(100)) < 0) {
            errors.add(ValidationError.of("step3", "HIGH priority requires estimated value >= 100"));
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        this.status = Status.SUBMITTED;
        this.submittedAt = Instant.now();
        this.updatedAt = Instant.now();

        return Result.ok();
    }

    public Result<Void> startAnalysis(Long analystId) {
        if (this.status != Status.SUBMITTED) {
            return Result.fail("Only SUBMITTED solicitations can start analysis");
        }
        this.status = Status.IN_REVIEW;
        this.updatedAt = Instant.now();
        return Result.ok();
    }

    public Result<Void> decide(boolean approve, Long analystId, String comment) {
        if (!status.canAnalyze()) {
            return Result.fail("Solicitation is not in analysis phase. Current status: " + status);
        }
        if (comment == null || comment.trim().length() < 10 || comment.trim().length() > 1000) {
            return Result.fail("Comment must be between 10 and 1000 characters");
        }

        this.status = approve ? Status.APPROVED : Status.REJECTED;
        this.analyzedAt = Instant.now();
        this.analyzedBy = analystId;
        this.analysisComment = comment.trim();
        this.updatedAt = Instant.now();

        return Result.ok();
    }

    private boolean isStep2Complete() {
        return cep != null &&
                street != null && !street.trim().isEmpty() &&
                number != null && !number.trim().isEmpty() &&
                neighborhood != null && !neighborhood.trim().isEmpty() &&
                city != null && !city.trim().isEmpty() &&
                state != null && state.trim().matches("^[A-Z]{2}$");
    }

    // ===== CLASSES INTERNAS =====

    public static class Result<T> {
        private final T value;
        private final List<ValidationError> errors;
        private final boolean success;

        private Result(T value, List<ValidationError> errors) {
            this.value = value;
            this.errors = errors;
            this.success = errors == null || errors.isEmpty();
        }

        public static <T> Result<T> ok() {
            return new Result<>(null, List.of());
        }
        public static <T> Result<T> ok(T value) {
            return new Result<>(value, List.of());
        }
        public static <T> Result<T> fail(String error) {
            return new Result<>(null, List.of(ValidationError.of("global", error)));
        }
        public static <T> Result<T> fail(List<ValidationError> errors) {
            return new Result<>(null, errors);
        }
        public boolean isSuccess() { return success; }
        public boolean isFailure() { return !success; }
        public List<ValidationError> getErrors() { return errors; }
        public T getValue() { return value; }
    }

    public static class ValidationError {
        private final String field;
        private final String message;

        private ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        public static ValidationError of(String field, String message) {
            return new ValidationError(field, message);
        }
        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}