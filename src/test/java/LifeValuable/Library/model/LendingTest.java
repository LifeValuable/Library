package LifeValuable.Library.model;

import jakarta.validation.constraints.AssertTrue;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class LendingTest extends BaseModelTest<Lending> {
    @Test
    void whenLendingCreated_thenAllFieldsAreCorrectlySet() {
        Lending lending = new Lending();
        lending.setId(1L);
        lending.setBook(new Book());
        lending.setReader(new Reader());
        lending.setLendingDate(LocalDate.now().minusDays(10));
        lending.setDueDate(LocalDate.now().plusDays(10));
        lending.setReturnDate(LocalDate.now());
        lending.setStatus(LendingStatus.RETURNED);
    }

    @Test
    void whenSetNullBook_thenValidationFails() {
        Lending lending = new Lending();
        lending.setBook(null);
        assertThatPropertyIsNotValid(lending, "book");
    }

    @Test
    void whenSetNullReader_thenValidationFails() {
        Lending lending = new Lending();
        lending.setReader(null);
        assertThatPropertyIsNotValid(lending, "reader");
    }

    @Test
    void whenSetNullLendingDate_thenValidationFails() {
        Lending lending = new Lending();
        lending.setLendingDate(null);
        assertThatPropertyIsNotValid(lending, "lendingDate");
    }

    @Test
    void whenSetNullDueDate_thenValidationFails() {
        Lending lending = new Lending();
        lending.setDueDate(null);
        assertThatPropertyIsNotValid(lending, "dueDate");
    }

    @Test
    void whenSetNullReturnDate_thenPropertyIsCorrectlySet(){
        Lending lending = new Lending();
        lending.setReturnDate(null);
        assertThatPropertyIsValid(lending, "returnDate");
    }

    @Test
    void whenSetDueDateBeforeLendingDate_thenValidationFails() {
        Lending lending = new Lending();
        lending.setLendingDate(LocalDate.now());
        lending.setDueDate(LocalDate.now().minusDays(1));
        assertThatAnnotationTriggered(lending, AssertTrue.class);
    }

    @Test
    void whenSetReturnDateBeforeLendingDate_thenValidationFails() {
        Lending lending = new Lending();
        lending.setLendingDate(LocalDate.now());
        lending.setReturnDate(LocalDate.now().minusDays(1));
        assertThatAnnotationTriggered(lending, AssertTrue.class);
    }
}
