package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.genre.GenreDTO;
import LifeValuable.Library.model.Genre;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class GenreMapperTest {

    private final GenreMapper genreMapper = Mappers.getMapper(GenreMapper.class);

    private Genre testGenre;

    @BeforeEach
    void setUp() {
        testGenre = new Genre();
        testGenre.setId(5L);
        testGenre.setName("Драма");
        testGenre.setDescription("Серьезные жизненные истории");
    }

    @Test
    void whenMapGenreToDto_thenResultEqualsExpectedDto() {
        GenreDTO expectedDto = new GenreDTO(
            5L,
            "Драма",
            "Серьезные жизненные истории"
        );

        GenreDTO actualDto = genreMapper.toDto(testGenre);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapNullGenre_thenReturnsNull() {
        Genre nullGenre = null;

        GenreDTO actualDto = genreMapper.toDto(nullGenre);

        assertThat(actualDto).isNull();
    }

    @Test
    void whenMapGenreWithNullFields_thenDtoHasNullFields() {
        Genre genreWithNulls = new Genre();
        genreWithNulls.setId(6L);
        genreWithNulls.setName(null);
        genreWithNulls.setDescription("Описание есть");

        GenreDTO expectedDto = new GenreDTO(
                6L,
                null,
                "Описание есть"
        );

        GenreDTO actualDto = genreMapper.toDto(genreWithNulls);

        assertThat(actualDto).isEqualTo(expectedDto);
    }
}
