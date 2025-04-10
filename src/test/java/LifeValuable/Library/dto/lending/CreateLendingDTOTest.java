package LifeValuable.Library.dto.lending;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import LifeValuable.Library.dto.BaseDTOTest;

import static org.assertj.core.api.Assertions.assertThat;

class CreateLendingDTOTest extends BaseDTOTest<CreateLendingDTO> {
    @Test
    void whenAllFieldsValid_thenValidationPasses() {
        CreateLendingDTO dto = new CreateLendingDTO(
            1L,
            1L,
            LocalDate.now(),
            LocalDate.now().plusDays(14)
        );

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenBookIdIsNull_thenValidationFails() {
        CreateLendingDTO dto = new CreateLendingDTO(
            null,
            1L,
            LocalDate.now(),
            LocalDate.now().plusDays(14)
        );

        assertThatPropertyIsNotValid(dto, "bookId");
    }

    @Test
    void whenReaderIdIsNull_thenValidationFails() {
        CreateLendingDTO dto = new CreateLendingDTO(
            1L,
            null,
            LocalDate.now(),
            LocalDate.now().plusDays(14)
        );

        assertThatPropertyIsNotValid(dto, "readerId");
    }

    @Test
    void whenLendingDateIsNull_thenValidationFails() {
        CreateLendingDTO dto = new CreateLendingDTO(
            1L,
            1L,
            null,
            LocalDate.now().plusDays(14)
        );

        assertThatPropertyIsNotValid(dto, "lendingDate");
    }

    @Test
    void whenLendingDateInFuture_thenValidationFails() {
        CreateLendingDTO dto = new CreateLendingDTO(
                1L,
                1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)
        );

        assertThatPropertyIsNotValid(dto, "lendingDate");
    }

    @Test
    void whenDueDateIsNull_thenValidationFails() {
        CreateLendingDTO dto = new CreateLendingDTO(
            1L,
            1L,
            LocalDate.now(),
            null
        );

        assertThatPropertyIsNotValid(dto, "dueDate");
    }
} 