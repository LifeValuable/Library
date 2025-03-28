package LifeValuable.Library.model;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Reader {
    @NotNull Long id;
    @NotBlank String firstName;
    @NotBlank String lastName;
    @NotBlank @Email String email;
    @Pattern(regexp = "^(\\+[1-9][0-9]{7,14})?$") String phoneNumber;
    @NotNull LocalDate registrationDate;

    @AssertTrue
    private boolean isRegistrationDateValid() {
        return registrationDate != null && !registrationDate.isAfter(LocalDate.now());
    }
}
