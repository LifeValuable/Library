package LifeValuable.Library.dto;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public abstract class BaseDTOTest<T> {
    protected Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    protected void assertThatPropertyIsValid(T dto, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(dto, propertyName);
        assertThat(violations)
            .as("No validation violations are expected for property '%s'", propertyName)
            .isEmpty();
    }

    protected void assertThatPropertyIsNotValid(T dto, String propertyName) {
        Set<ConstraintViolation<T>> violations = validator.validateProperty(dto, propertyName);
        assertThat(violations)
            .as("Expected validation violation for property '%s'", propertyName)
            .hasSize(1);
    }
} 