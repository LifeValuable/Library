package LifeValuable.Library.model;

import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class Lending {
    @NotNull Long id;
    @NotNull Book book;
    @NotNull Reader reader;
    @NotNull LocalDate lendingDate;
    @NotNull LocalDate dueDate;
    LocalDate returnDate;
    LendingStatus status;

    @AssertTrue
    private boolean isDueDateValid() {
        return dueDate != null && lendingDate != null && !dueDate.isBefore(lendingDate);
    }

    @AssertTrue
    private boolean isReturnDateValid() {
        return returnDate == null || lendingDate != null && !returnDate.isBefore(lendingDate);
    }
}
