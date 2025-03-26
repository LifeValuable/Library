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
}
