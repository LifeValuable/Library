package LifeValuable.Library.dto.lending;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record CreateLendingDTO(
    @NotNull Long bookId,
    @NotNull Long readerId,
    @NotNull @PastOrPresent LocalDate lendingDate,
    @NotNull LocalDate dueDate) {
}
