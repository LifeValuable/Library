package LifeValuable.Library.dto.reader;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateReaderDTO(
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank @Email String email,
    @Pattern(regexp = "^(\\+[1-9][0-9]{7,14})?$") String phoneNumber) {
}
