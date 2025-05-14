package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.model.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LendingMapperTest {

    private final LendingMapper lendingMapper = Mappers.getMapper(LendingMapper.class);

    private Book testBook;
    private Reader testReader;
    private Lending activeLending;
    private Lending overdueLending;
    private Lending returnedLending;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        Genre defaultGenre = new Genre();
        defaultGenre.setId(1L);
        defaultGenre.setName("ТестЖанр");

        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Тестовая Книга");
        testBook.setIsbn("123-456");
        testBook.setAuthor("Автор");
        testBook.setPublicationYear(2023);
        testBook.setStock(5);
        testBook.setGenres(new ArrayList<>(List.of(defaultGenre)));
        testBook.setLendings(new ArrayList<>());

        testReader = new Reader();
        testReader.setId(1L);
        testReader.setFirstName("Иван");
        testReader.setLastName("Тестовый");
        testReader.setEmail("ivan@test.com");
        testReader.setPhoneNumber("+79991112233");
        testReader.setRegistrationDate(today.minusMonths(1));
        testReader.setLendings(new ArrayList<>());

        activeLending = new Lending();
        activeLending.setId(10L);
        activeLending.setBook(testBook);
        activeLending.setReader(testReader);
        activeLending.setLendingDate(today.minusDays(5));
        activeLending.setDueDate(today.plusDays(10));
        activeLending.setStatus(LendingStatus.ACTIVE);
        activeLending.setReturnDate(null);

        overdueLending = new Lending();
        overdueLending.setId(11L);
        overdueLending.setBook(testBook);
        overdueLending.setReader(testReader);
        overdueLending.setLendingDate(today.minusDays(20));
        overdueLending.setDueDate(today.minusDays(5));
        overdueLending.setStatus(LendingStatus.OVERDUE);
        overdueLending.setReturnDate(null);

        returnedLending = new Lending();
        returnedLending.setId(12L);
        returnedLending.setBook(testBook);
        returnedLending.setReader(testReader);
        returnedLending.setLendingDate(today.minusDays(15));
        returnedLending.setDueDate(today.minusDays(1));
        returnedLending.setStatus(LendingStatus.RETURNED);
        returnedLending.setReturnDate(today.minusDays(2));
    }

    @Test
    void whenMapActiveLendingToDto_thenResultEqualsExpectedDto() {
        long days = ChronoUnit.DAYS.between(today, activeLending.getDueDate());
        Integer expectedDaysLeft = (int)(days < 0 ? 0 : days);

        LendingDTO expectedDto = new LendingDTO(
                10L,
                "Тестовая Книга",
                "Иван Тестовый",
                activeLending.getLendingDate(),
                activeLending.getDueDate(),
                LendingStatus.ACTIVE,
                false,
                expectedDaysLeft
        );

        LendingDTO actualDto = lendingMapper.toDto(activeLending);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapOverdueLendingToDto_thenResultEqualsExpectedDto() {
        long daysOverdue = ChronoUnit.DAYS.between(today, overdueLending.getDueDate());
        Integer expectedDaysLeft = (int) daysOverdue;

        LendingDTO expectedDto = new LendingDTO(
                11L,
                "Тестовая Книга",
                "Иван Тестовый",
                overdueLending.getLendingDate(),
                overdueLending.getDueDate(),
                LendingStatus.OVERDUE,
                true,
                expectedDaysLeft
        );

        LendingDTO actualDto = lendingMapper.toDto(overdueLending);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapReturnedLendingToDto_thenResultEqualsExpectedDto() {
        LendingDTO expectedDto = new LendingDTO(
                12L,
                "Тестовая Книга",
                "Иван Тестовый",
                returnedLending.getLendingDate(),
                returnedLending.getDueDate(),
                LendingStatus.RETURNED,
                false,
                0
        );

        LendingDTO actualDto = lendingMapper.toDto(returnedLending);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapLendingWithNullDueDateToDto_thenCalculatedFieldsAreDefault() {
        activeLending.setDueDate(null);
        LendingDTO expectedDto = new LendingDTO(
                10L,
                "Тестовая Книга",
                "Иван Тестовый",
                activeLending.getLendingDate(),
                null,
                LendingStatus.ACTIVE,
                false,
                0
        );

        LendingDTO actualDto = lendingMapper.toDto(activeLending);

        assertThat(actualDto).isEqualTo(expectedDto);
    }


    @Test
    void whenMapLendingToDetailDto_thenResultEqualsExpectedDetailDto() {
        LendingDetailDTO expectedDto = new LendingDetailDTO(
                10L,
                1L,
                "Тестовая Книга",
                "123-456",
                1L,
                "Иван Тестовый",
                "ivan@test.com",
                activeLending.getLendingDate(),
                activeLending.getDueDate(),
                activeLending.getReturnDate(),
                activeLending.getStatus()
        );

        LendingDetailDTO actualDto = lendingMapper.toDetailDto(activeLending);

        assertThat(actualDto).isEqualTo(expectedDto);
    }


    @Test
    void whenMapCreateDtoToEntity_thenResultMatchesExpectedEntityIgnoringSpecificFields() {
        LocalDate lendingDate = today.minusDays(1);
        LocalDate dueDate = today.plusDays(13);
        CreateLendingDTO createDto = new CreateLendingDTO(
                1L,
                1L,
                lendingDate,
                dueDate
        );

        Lending expectedLending = new Lending();
        expectedLending.setLendingDate(lendingDate);
        expectedLending.setDueDate(dueDate);
        expectedLending.setId(null);
        expectedLending.setBook(null);
        expectedLending.setReader(null);
        expectedLending.setStatus(null);
        expectedLending.setReturnDate(null);

        Lending actualEntity = lendingMapper.toEntity(createDto);

        assertThat(actualEntity).isNotNull();
        assertThat(actualEntity)
                .usingRecursiveComparison()
                .ignoringFields("id", "book", "reader", "status", "returnDate")
                .isEqualTo(expectedLending);
        assertThat(actualEntity.getId()).isNull();
        assertThat(actualEntity.getBook()).isNull();
        assertThat(actualEntity.getReader()).isNull();
        assertThat(actualEntity.getStatus()).isNull();
        assertThat(actualEntity.getReturnDate()).isNull();
    }

    @Test
    void whenMapNullLending_thenReturnsNull() {
        assertThat(lendingMapper.toDto(null)).isNull();
        assertThat(lendingMapper.toDetailDto(null)).isNull();
    }

    @Test
    void whenMapLendingWithNullReader_thenReaderFieldsAreEmptyOrDefault() {
        activeLending.setReader(null);

        LendingDTO dto = lendingMapper.toDto(activeLending);
        LendingDetailDTO detailDto = lendingMapper.toDetailDto(activeLending);

        assertThat(dto.readerFullName()).isEqualTo("");
        assertThat(detailDto.readerFullName()).isEqualTo("");
        assertThat(detailDto.readerId()).isNull();
        assertThat(detailDto.readerEmail()).isNull();
    }

    @Test
    void whenMapLendingWithNullBook_thenBookFieldsAreNull() {
        activeLending.setBook(null);

        LendingDTO dto = lendingMapper.toDto(activeLending);
        LendingDetailDTO detailDto = lendingMapper.toDetailDto(activeLending);

        assertThat(dto.bookTitle()).isNull();
        assertThat(detailDto.bookTitle()).isNull();
        assertThat(detailDto.bookId()).isNull();
        assertThat(detailDto.bookIsbn()).isNull();
    }
}
