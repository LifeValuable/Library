package LifeValuable.Library.dto.book;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import LifeValuable.Library.model.Book;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.Test;

import LifeValuable.Library.dto.BaseDTOTest;

import static org.assertj.core.api.Assertions.assertThat;

class CreateBookDTOTest extends BaseDTOTest<CreateBookDTO> {
    @Test
    void whenAllFieldsValid_thenValidationPasses() {
        CreateBookDTO dto = new CreateBookDTO(
            "Война и мир",
            "Лев Толстой",
            "978-5-389-21519-1",
            1869,
            10,
            Arrays.asList("Роман", "Классика")
        );

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenTitleIsBlank_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "",
            "Федор Достоевский",
            "978-5-389-21519-1",
            1866,
            5,
            List.of("Роман")
        );

        assertThatPropertyIsNotValid(dto, "title");
    }

    @Test
    void whenAuthorIsBlank_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "Преступление и наказание",
            "",
            "978-5-389-21519-1",
            1866,
            5,
            List.of("Роман")
        );

        assertThatPropertyIsNotValid(dto, "author");
    }

    @Test
    void whenIsbnIsInvalid_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "Мастер и Маргарита",
            "Михаил Булгаков",
            "неверный-isbn",
            1967,
            5,
            Arrays.asList("Роман", "Фантастика")
        );

        assertThatPropertyIsNotValid(dto, "isbn");
    }

    @Test
    void whenPublicationYearIsNegative_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "Евгений Онегин",
            "Александр Пушкин",
            "978-5-389-21519-1",
            -1,
            5,
            List.of("Поэма")
        );

        assertThatPropertyIsNotValid(dto, "publicationYear");
    }

    @Test
    void whenStockIsNegative_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "Мёртвые души",
            "Николай Гоголь",
            "978-5-389-21519-1",
            1842,
            -1,
            List.of("Роман")
        );

        assertThatPropertyIsNotValid(dto, "stock");
    }

    @Test
    void whenGenreListIsEmpty_thenValidationFails() {
        CreateBookDTO dto = new CreateBookDTO(
            "Анна Каренина",
            "Лев Толстой",
            "978-5-389-21519-1",
            1877,
            5,
            Collections.emptyList()
        );

        assertThatPropertyIsNotValid(dto, "genreNames");
    }
} 