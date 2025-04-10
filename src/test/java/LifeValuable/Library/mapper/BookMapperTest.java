package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private Book testBook;
    private Genre genre1;
    private Genre genre2;
    private Lending activeLending;
    private Lending overdueLending;
    private Lending returnedLending;

    @BeforeEach
    void setUp() {
        genre1 = new Genre();
        genre1.setId(1L);
        genre1.setName("Фантастика");

        genre2 = new Genre();
        genre2.setId(2L);
        genre2.setName("Приключения");

        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Хроники Нарнии");
        testBook.setAuthor("Клайв Льюис");
        testBook.setIsbn("978-5-699-14902-6");
        testBook.setPublicationYear(1950);
        testBook.setStock(5);
        testBook.setGenres(new ArrayList<>(Arrays.asList(genre1, genre2)));

        activeLending = new Lending();
        activeLending.setId(1L);
        activeLending.setBook(testBook);
        activeLending.setStatus(LendingStatus.ACTIVE);

        overdueLending = new Lending();
        overdueLending.setId(2L);
        overdueLending.setBook(testBook);
        overdueLending.setStatus(LendingStatus.OVERDUE);

        returnedLending = new Lending();
        returnedLending.setId(3L);
        returnedLending.setBook(testBook);
        returnedLending.setStatus(LendingStatus.RETURNED);

        testBook.setLendings(new ArrayList<>(Arrays.asList(activeLending, overdueLending, returnedLending)));
    }

    @Test
    void whenMapBookToDto_thenResultEqualsExpectedDto() {
        BookDTO expectedDto = new BookDTO(
                10L,
                "Хроники Нарнии",
                "Клайв Льюис",
                1950,
                5,
                Arrays.asList("Фантастика", "Приключения")
        );

        BookDTO actualDto = bookMapper.toDto(testBook);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapBookToDetailDto_thenResultEqualsExpectedDetailDto() {
        BookDetailDTO expectedDetailDto = new BookDetailDTO(
                10L,
                "Хроники Нарнии",
                "Клайв Льюис",
                "978-5-699-14902-6",
                1950,
                5,
                3,
                Arrays.asList("Фантастика", "Приключения")
        );

        BookDetailDTO actualDetailDto = bookMapper.toDetailDto(testBook);

        assertThat(actualDetailDto).isEqualTo(expectedDetailDto);
    }

    @Test
    void whenMapBookToDetailDto_withNoLendings_thenAvailableStockEqualsStockAndResultEqualsExpected() {
        testBook.setLendings(new ArrayList<>());
        BookDetailDTO expectedDetailDto = new BookDetailDTO(
                10L,
                "Хроники Нарнии",
                "Клайв Льюис",
                "978-5-699-14902-6",
                1950,
                5,
                5,
                Arrays.asList("Фантастика", "Приключения")
        );

        BookDetailDTO actualDetailDto = bookMapper.toDetailDto(testBook);

        assertThat(actualDetailDto).isEqualTo(expectedDetailDto);
        assertThat(actualDetailDto.availableStock()).isEqualTo(testBook.getStock());
    }

    @Test
    void whenMapBookToDetailDto_withOnlyReturnedLendings_thenAvailableStockEqualsStockAndResultEqualsExpected() {
        testBook.setLendings(new ArrayList<>(Collections.singletonList(returnedLending)));
        BookDetailDTO expectedDetailDto = new BookDetailDTO(
                10L,
                "Хроники Нарнии",
                "Клайв Льюис",
                "978-5-699-14902-6",
                1950,
                5,
                5,
                Arrays.asList("Фантастика", "Приключения")
        );

        BookDetailDTO actualDetailDto = bookMapper.toDetailDto(testBook);

        assertThat(actualDetailDto).isEqualTo(expectedDetailDto);
        assertThat(actualDetailDto.availableStock()).isEqualTo(testBook.getStock());
    }


    @Test
    void whenMapCreateDtoToEntity_thenFieldsAreMappedAndIgnoredFieldsAreNullOrEmpty() {
        CreateBookDTO createDto = new CreateBookDTO(
                "Новая Книга",
                "Новый Автор",
                "111-2223334445",
                2024,
                10,
                Arrays.asList("Наука", "Популярное")
        );

        Book entity = bookMapper.toEntity(createDto);

        assertThat(entity).isNotNull();
        assertThat(entity.getTitle()).isEqualTo(createDto.title());
        assertThat(entity.getAuthor()).isEqualTo(createDto.author());
        assertThat(entity.getIsbn()).isEqualTo(createDto.isbn());
        assertThat(entity.getPublicationYear()).isEqualTo(createDto.publicationYear());
        assertThat(entity.getStock()).isEqualTo(createDto.stock());
        assertThat(entity.getId()).isNull();
        assertThat(entity.getGenres()).isNull();
        assertThat(entity.getLendings()).isNull();
    }

    @Test
    void whenMapBookListToDtoList_thenListIsCorrectlyMapped() {
        Book book2 = new Book();
        book2.setId(11L);
        book2.setTitle("Вторая Книга");
        book2.setAuthor("Другой Автор");
        book2.setStock(3);
        book2.setGenres(new ArrayList<>(Collections.singletonList(genre1)));
        book2.setLendings(new ArrayList<>());

        List<Book> bookList = Arrays.asList(testBook, book2);

        List<BookDTO> expectedDtoList = List.of(
                new BookDTO(10L, "Хроники Нарнии", "Клайв Льюис", 1950, 5, Arrays.asList("Фантастика", "Приключения")),
                new BookDTO(11L, "Вторая Книга", "Другой Автор", null, 3, Arrays.asList("Фантастика"))
        );


        List<BookDTO> actualDtoList = bookMapper.toDtoList(bookList);

        assertThat(actualDtoList).isEqualTo(expectedDtoList);
    }

    @Test
    void whenMapNullBook_thenNamedMethodsHandleNullGracefully() {
        BookDTO dto = bookMapper.toDto(null);
        assertThat(dto).isNull();

        BookDetailDTO detailDto = bookMapper.toDetailDto(null);
        assertThat(detailDto).isNull();

        List<String> genreNames = bookMapper.getGenreNames(null);
        long availableStock = bookMapper.getAvailableStock(null);

        assertThat(genreNames).isNotNull().isEmpty();
        assertThat(availableStock).isZero();
    }

    @Test
    void whenMapBookWithNullGenresOrLendings_thenNamedMethodsHandleNullGracefully() {
        testBook.setGenres(null);
        testBook.setLendings(null);

        BookDTO dto = bookMapper.toDto(testBook);
        BookDetailDTO detailDto = bookMapper.toDetailDto(testBook);

        assertThat(dto.genreNames()).isNotNull().isEmpty();
        assertThat(detailDto.genreNames()).isNotNull().isEmpty();
        assertThat(detailDto.availableStock()).isEqualTo(testBook.getStock());
    }
}
