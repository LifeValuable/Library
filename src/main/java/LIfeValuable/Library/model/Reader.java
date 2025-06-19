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
    private Long id;
    @NotBlank private  String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    @Pattern(regexp = "^(\\+[1-9][0-9]{7,14})?$") private String phoneNumber;
    @NotNull private LocalDate registrationDate;

    @NotBlank private String password;

    @Enumerated(EnumType.STRING)
    @NotNull private Role role = Role.READER;

    @OneToMany(mappedBy = "reader", fetch = FetchType.LAZY)
    private List<Lending> lendings;

    @AssertTrue
    private boolean isRegistrationDateValid() {
        return registrationDate != null && !registrationDate.isAfter(LocalDate.now());
    }
}
