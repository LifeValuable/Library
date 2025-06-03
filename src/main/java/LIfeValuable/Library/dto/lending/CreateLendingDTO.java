package LifeValuable.Library.dto.lending;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

public record CreateLendingDTO(
    @NotNull Long bookId,
    @NotNull Long readerId,
    @NotNull @PastOrPresent @JsonFormat(pattern = "yyyy-MM-dd") LocalDate lendingDate,
    @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dueDate) {
}
