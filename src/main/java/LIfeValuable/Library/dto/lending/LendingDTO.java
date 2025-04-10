package LifeValuable.Library.dto.lending;

import LifeValuable.Library.model.LendingStatus;

import java.time.LocalDate;

public record LendingDTO(
    Long id,
    String bookTitle,
    String readerFullName,
    LocalDate lendingDate,
    LocalDate dueDate,
    LendingStatus status,
    Boolean isOverdue,
    Integer daysLeft) {
}
