package LifeValuable.Library.dto.lending;

import LifeValuable.Library.model.LendingStatus;

import java.time.LocalDate;

public record LendingDetailDTO(
    Long id,
    Long bookId,
    String bookTitle,
    String bookIsbn,
    Long readerId,
    String readerFullName,
    String readerEmail,
    LocalDate lendingDate,
    LocalDate dueDate,
    LocalDate returnDate,
    LendingStatus status) {
}
