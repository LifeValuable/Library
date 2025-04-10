package LifeValuable.Library.dto.reader;

import java.time.LocalDate;

public record ReaderDTO(
    Long id,
    String fullName,
    String email,
    String phoneNumber,
    LocalDate registrationDate,
    Integer activeLendingsCount) {
}
