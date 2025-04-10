package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.model.Reader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReaderMapperTest {

    private final ReaderMapper readerMapper = Mappers.getMapper(ReaderMapper.class);

    private Reader testReader;
    private Lending activeLending1;
    private Lending activeLending2;
    private Lending overdueLending;
    private Lending returnedLending;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now();

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Sample Book");
        book.setAuthor("Author");
        book.setStock(10);
        Genre g = new Genre(); g.setId(1L); g.setName("Genre");
        book.setGenres(new ArrayList<>(List.of(g)));
        book.setLendings(new ArrayList<>());


        testReader = new Reader();
        testReader.setId(1L);
        testReader.setFirstName("Анна");
        testReader.setLastName("Каренина");
        testReader.setEmail("anna.k@example.com");
        testReader.setPhoneNumber("+79112223344");
        testReader.setRegistrationDate(today.minusYears(1));

        activeLending1 = new Lending();
        activeLending1.setId(1L);
        activeLending1.setReader(testReader);
        activeLending1.setBook(book);
        activeLending1.setStatus(LendingStatus.ACTIVE);
        activeLending1.setLendingDate(today.minusDays(10));
        activeLending1.setDueDate(today.plusDays(4));

        activeLending2 = new Lending();
        activeLending2.setId(2L);
        activeLending2.setReader(testReader);
        activeLending2.setBook(book);
        activeLending2.setStatus(LendingStatus.ACTIVE);
        activeLending2.setLendingDate(today.minusDays(2));
        activeLending2.setDueDate(today.plusDays(12));

        overdueLending = new Lending();
        overdueLending.setId(3L);
        overdueLending.setReader(testReader);
        overdueLending.setBook(book);
        overdueLending.setStatus(LendingStatus.OVERDUE);
        overdueLending.setLendingDate(today.minusDays(30));
        overdueLending.setDueDate(today.minusDays(16));

        returnedLending = new Lending();
        returnedLending.setId(4L);
        returnedLending.setReader(testReader);
        returnedLending.setBook(book);
        returnedLending.setStatus(LendingStatus.RETURNED);
        returnedLending.setLendingDate(today.minusDays(60));
        returnedLending.setDueDate(today.minusDays(46));
        returnedLending.setReturnDate(today.minusDays(40));

        testReader.setLendings(new ArrayList<>(Arrays.asList(
                activeLending1, activeLending2, overdueLending, returnedLending
        )));
    }

    @Test
    void whenMapReaderToDto_thenResultEqualsExpectedDto() {
        ReaderDTO expectedDto = new ReaderDTO(
                1L,
                "Анна Каренина",
                "anna.k@example.com",
                "+79112223344",
                testReader.getRegistrationDate(),
                2
        );

        ReaderDTO actualDto = readerMapper.toDto(testReader);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapReaderToDetailDto_thenResultEqualsExpectedDto() {
        ReaderDetailDTO expectedDto = new ReaderDetailDTO(
                1L,
                "Анна",
                "Каренина",
                "anna.k@example.com",
                "+79112223344",
                testReader.getRegistrationDate(),
                2,
                1,
                4 
        );

        ReaderDetailDTO actualDto = readerMapper.toDetailDto(testReader);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapReaderWithNoLendingsToDto_thenCountsAreZero() {
        testReader.setLendings(new ArrayList<>());
        ReaderDTO expectedDto = new ReaderDTO(
                1L,
                "Анна Каренина",
                "anna.k@example.com",
                "+79112223344",
                testReader.getRegistrationDate(),
                0
        );

        ReaderDTO actualDto = readerMapper.toDto(testReader);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapReaderWithNoLendingsToDetailDto_thenCountsAreZero() {
        testReader.setLendings(new ArrayList<>());
        ReaderDetailDTO expectedDto = new ReaderDetailDTO(
                1L,
                "Анна",
                "Каренина",
                "anna.k@example.com",
                "+79112223344",
                testReader.getRegistrationDate(),
                0,
                0,
                0 
        );

        ReaderDetailDTO actualDto = readerMapper.toDetailDto(testReader);

        assertThat(actualDto).isEqualTo(expectedDto);
    }

    @Test
    void whenMapReaderWithNullLendingsToDto_thenCountsAreZero() {
        testReader.setLendings(null);
        ReaderDTO expectedDto = new ReaderDTO(
                1L,
                "Анна Каренина",
                "anna.k@example.com",
                "+79112223344",
                testReader.getRegistrationDate(),
                0
        );

        ReaderDTO actualDto = readerMapper.toDto(testReader);

        assertThat(actualDto).isEqualTo(expectedDto);
    }


    @Test
    void whenMapCreateDtoToEntity_thenResultMatchesExpectedEntityIgnoringSpecificFields() {
        CreateReaderDTO createDto = new CreateReaderDTO(
                "Лев",
                "Толстой",
                "leo.t@example.com",
                "+79223334455"
        );

        Reader expectedReader = new Reader();
        expectedReader.setFirstName("Лев");
        expectedReader.setLastName("Толстой");
        expectedReader.setEmail("leo.t@example.com");
        expectedReader.setPhoneNumber("+79223334455");
        expectedReader.setRegistrationDate(today);
        expectedReader.setId(null);
        expectedReader.setLendings(null);

        Reader actualReader = readerMapper.toEntity(createDto);

        assertThat(actualReader).isNotNull();
        assertThat(actualReader)
                .isEqualToIgnoringGivenFields(expectedReader, "id", "lendings", "registrationDate");
        assertThat(actualReader.getId()).isNull();
        assertThat(actualReader.getLendings()).isNull();
        assertThat(actualReader.getRegistrationDate()).isEqualTo(today);
    }

    @Test
    void whenMapReaderListToDtoList_thenListIsCorrectlyMapped() {
        Reader reader2 = new Reader();
        reader2.setId(2L);
        reader2.setFirstName("Федор");
        reader2.setLastName("Достоевский");
        reader2.setEmail("fedor.d@example.com");
        reader2.setLendings(new ArrayList<>());
        reader2.setRegistrationDate(today.minusMonths(6));

        List<Reader> readers = Arrays.asList(testReader, reader2);

        List<ReaderDTO> expectedDtoList = List.of(
                new ReaderDTO(1L, "Анна Каренина", "anna.k@example.com", "+79112223344", testReader.getRegistrationDate(), 2),
                new ReaderDTO(2L, "Федор Достоевский", "fedor.d@example.com", null, reader2.getRegistrationDate(), 0)
        );

        List<ReaderDTO> actualDtoList = readerMapper.toDtoList(readers);

        assertThat(actualDtoList).isEqualTo(expectedDtoList);
    }

    @Test
    void whenMapNullReader_thenReturnsNull() {
        assertThat(readerMapper.toDto(null)).isNull();
        assertThat(readerMapper.toDetailDto(null)).isNull();
    }

    @Test
    void whenGetFullNameCalledWithNullReader_thenReturnsEmptyString() {
        String fullName = readerMapper.getFullName(null);
        assertThat(fullName).isEqualTo("");
    }

    @Test
    void whenCountMethodsCalledWithNullReaderOrLendings_thenReturnsZero() {
        assertThat(readerMapper.countActiveLendings(null)).isZero();
        assertThat(readerMapper.countOverdueLendings(null)).isZero();
        assertThat(readerMapper.countTotalLendings(null)).isZero();

        testReader.setLendings(null);
        assertThat(readerMapper.countActiveLendings(testReader)).isZero();
        assertThat(readerMapper.countOverdueLendings(testReader)).isZero();
        assertThat(readerMapper.countTotalLendings(testReader)).isZero();
    }
}
