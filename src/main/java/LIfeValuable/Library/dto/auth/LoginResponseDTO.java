package LifeValuable.Library.dto.auth;

public record LoginResponseDTO(
        String message,
        String role,
        String firstName,
        String lastName,
        String email
) {}
