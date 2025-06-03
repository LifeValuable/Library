package LifeValuable.Library.dto.exception;

public record FieldViolation(
        String field,
        Object rejectedValue,
        String message
) {}
