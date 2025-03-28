package LifeValuable.Library.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseModelTest<T> {
    protected Validator validator;

    protected void assertThatAnnotationTriggered(T object, Class<?> annotation) {
        Set<ConstraintViolation<T>> violations =
                validator.validate(object);
        boolean assertTrueTriggered = violations.stream()
                .anyMatch(v -> v.getConstraintDescriptor().getAnnotation().annotationType().equals(annotation));
        assertThat(assertTrueTriggered)
                .as("Expected annotation %s to be triggered for object of type %s",
                        annotation.getSimpleName(), object.getClass().getSimpleName())
                .isTrue();
    }

    protected void assertThatPropertyIsNotValid(T object, String property) {
        Set<ConstraintViolation<T>> violations =
                validator.validateProperty(object, property);
        assertThat(violations)
                .as("Expected validation violation for property '%s'", property)
                .hasSize(1);
    }

    protected void assertThatPropertyIsValid(T object, String property) {
        Set<ConstraintViolation<T>> violations =
                validator.validateProperty(object, property);
        assertThat(violations)
                .as("No validation violations are expected for property '%s'", property)
                .hasSize(0);
    }

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }
}
