package LifeValuable.Library.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Reader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @NotBlank String firstName;
    @NotBlank String lastName;
    @NotBlank @Email String email;
    @Pattern(regexp = "^(\\+[1-9][0-9]{7,14})?$") String phoneNumber;
    @NotNull LocalDate registrationDate;

    @OneToMany(mappedBy = "reader", fetch = FetchType.LAZY)
    private List<Lending> lendings;

    @AssertTrue
    private boolean isRegistrationDateValid() {
        return registrationDate != null && !registrationDate.isAfter(LocalDate.now());
    }
}
