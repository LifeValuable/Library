package LifeValuable.Library.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookTest extends BaseModelTest<Book>{

    @Test
    void whenBookCreated_thenFieldsAreCorrectlySet() {
        Book book = new Book();
        List<Genre> genres = new ArrayList<>();
        genres.add(new Genre());

        book.setId(1L);
        book.setTitle("Война и мир");
        book.setAuthor("Лев Толстой");
        book.setIsbn("978-5-389-21519-1");
        book.setGenres(genres);
        book.setPublicationYear(2024);
        book.setStock(1000);

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenSetBlankTitle_thenValidationFails() {
        Book book = new Book();
        book.setTitle("");
        assertThatPropertyIsNotValid(book, "title");
    }

    @Test
    void whenSetBlankAuthor_thenValidationFails() {
        Book book = new Book();
        book.setAuthor("");
        assertThatPropertyIsNotValid(book, "author");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "978-5-21519-1", "78-5-389-21519-1", "978-5-389-21519-10"})
    void whenSetWrongIsbn_thenValidationFails(String isbn) {
        Book book = new Book();
        book.setIsbn(isbn);
        assertThatPropertyIsNotValid(book, "isbn");
    }


    @Test
    void whenSetPublicationYearInFuture_thenValidationFails() {
        Book book = new Book();

        assertThrows(IllegalArgumentException.class, () ->
                book.setPublicationYear(LocalDate.now().getYear() + 1));
    }


    @Test
    void whenSetNegativePublicationYear_thenValidationFails() {
        Book book = new Book();
        assertThrows(IllegalArgumentException.class, () ->
                book.setPublicationYear(-1));
    }


    @Test
    void whenSetEmptyGenres_thenValidationFails() {
        Book book = new Book();
        book.setGenres(new ArrayList<>());
        assertThatPropertyIsNotValid(book, "genres");
    }


    @Test
    void whenSetNegativeStock_thenValidationFails() {
        Book book = new Book();
        book.setStock(-1);
        assertThatPropertyIsNotValid(book, "stock");
    }

}
