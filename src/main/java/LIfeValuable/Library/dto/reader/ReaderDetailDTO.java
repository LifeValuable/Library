package LifeValuable.Library.dto.reader;

import java.time.LocalDate;

public record ReaderDetailDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    LocalDate registrationDate,
    Integer activeLendingsCount,
    Integer overdueLendingsCount,
    Integer totalBorrowed,
    String role) {
}
