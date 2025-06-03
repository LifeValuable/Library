package LifeValuable.Library.dto.lending;

import LifeValuable.Library.model.LendingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record LendingDTO(
    Long id,
    String bookTitle,
    String readerFullName,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lendingDate,
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
    LendingStatus status,
    Boolean isOverdue,
    Integer daysLeft) {
}
