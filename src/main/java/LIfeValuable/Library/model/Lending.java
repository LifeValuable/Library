package LifeValuable.Library.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
public class Lending {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    Book book;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "reader_id", nullable = false)
    Reader reader;

    @NotNull LocalDate lendingDate;

    @NotNull LocalDate dueDate;

    LocalDate returnDate;

    @Enumerated(EnumType.STRING)
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
