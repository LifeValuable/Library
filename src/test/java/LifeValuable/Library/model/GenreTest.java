package LifeValuable.Library.model;

import org.junit.jupiter.api.Test;

public class GenreTest extends BaseModelTest<Genre> {

    @Test
    void whenGenreCreated_thenFieldsAreCorrectlySet() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Ужасы");
        genre.setDescription("Страшное кино");
    }

    @Test
    void whenSetNullId_thenValidationFails() {
        Genre genre = new Genre();
        genre.setId(null);
        assertThatPropertyIsNotValid(genre, "id");
    }

    @Test
    void whenSetBlankName_thenValidationFails() {
        Genre genre = new Genre();
        genre.setName("");
        assertThatPropertyIsNotValid(genre, "name");
    }

    @Test
    void whenSetBlankDescription_thenValidationFails() {
        Genre genre = new Genre();
        genre.setDescription("");
        assertThatPropertyIsNotValid(genre, "description");
    }
}
