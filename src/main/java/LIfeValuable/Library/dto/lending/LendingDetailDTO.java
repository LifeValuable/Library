package LifeValuable.Library.dto.lending;

import LifeValuable.Library.model.LendingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record LendingDetailDTO(
    Long id,
    Long bookId,
    String bookTitle,
    String bookIsbn,
    Long readerId,
    String readerFullName,
    String readerEmail,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lendingDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate returnDate,
    LendingStatus status) {
}
