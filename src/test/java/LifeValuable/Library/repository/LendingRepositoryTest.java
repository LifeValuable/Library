package LifeValuable.Library.repository;


import LifeValuable.Library.config.DataConfig;
import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(DataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class LendingRepositoryTest {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LendingRepository lendingRepository;

    private Book book;
    private Book book2;
    private Book book3;
    private Reader reader;
    private Reader reader2;
    private Lending lending;
    private Lending lending2;
    private Lending lending3;
    private Lending lending4;

    private Pageable defaultPageable;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Lending").executeUpdate();
        entityManager.createQuery("DELETE FROM Book").executeUpdate();
        entityManager.createQuery("DELETE FROM Reader").executeUpdate();
        entityManager.createQuery("DELETE FROM Genre").executeUpdate();

        entityManager.createNativeQuery("ALTER TABLE lending ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE book ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reader ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE genre ALTER COLUMN id RESTART WITH 1").executeUpdate();

        Genre genre = new Genre();
        genre.setName("Классика");
        genre.setDescription("Классическая литература");
        entityManager.persist(genre);

        book = new Book();
        book.setTitle("Мастер и Маргарита");
        book.setAuthor("Михаил Булгаков");
        book.setIsbn("9785699236916");
        book.setPublicationYear(1967);
        book.setStock(5);
        List<Genre> genres = new ArrayList<>();
        genres.add(genre);
        book.setGenres(genres);
        entityManager.persist(book);

        book2 = new Book();
        book2.setTitle("Война и мир");
        book2.setAuthor("Лев Толстой");
        book2.setIsbn("9785389054097");
        book2.setPublicationYear(1869);
        book2.setStock(3);
        book2.setGenres(new ArrayList<>(genres));
        entityManager.persist(book2);

        book3 = new Book();
        book3.setTitle("Преступление и наказание");
        book3.setAuthor("Федор Достоевский");
        book3.setIsbn("9785170878895");
        book3.setPublicationYear(1866);
        book3.setStock(4);
        book3.setGenres(new ArrayList<>(genres));
        entityManager.persist(book3);

        reader = new Reader();
        reader.setFirstName("Иван");
        reader.setLastName("Петров");
        reader.setEmail("ipetrov@example.com");
        reader.setPhoneNumber("+79991234567");
        reader.setRegistrationDate(LocalDate.now().minusMonths(1));
        entityManager.persist(reader);

        reader2 = new Reader();
        reader2.setFirstName("Мария");
        reader2.setLastName("Сидорова");
        reader2.setEmail("msidorova@example.com");
        reader2.setPhoneNumber("+79997654321");
        reader2.setRegistrationDate(LocalDate.now().minusMonths(2));
        entityManager.persist(reader2);

        lending = new Lending();
        lending.setBook(book);
        lending.setReader(reader);
        lending.setLendingDate(LocalDate.now().minusDays(7));
        lending.setDueDate(LocalDate.now().plusDays(7));
        lending.setStatus(LendingStatus.ACTIVE);
        entityManager.persist(lending);

        lending2 = new Lending();
        lending2.setBook(book2);
        lending2.setReader(reader);
        lending2.setLendingDate(LocalDate.now().minusDays(21));
        lending2.setDueDate(LocalDate.now().minusDays(7));
        lending2.setStatus(LendingStatus.OVERDUE);
        entityManager.persist(lending2);

        lending3 = new Lending();
        lending3.setBook(book);
        lending3.setReader(reader2);
        lending3.setLendingDate(LocalDate.now().minusDays(30));
        lending3.setDueDate(LocalDate.now().minusDays(16));
        lending3.setReturnDate(LocalDate.now().minusDays(20));
        lending3.setStatus(LendingStatus.RETURNED);
        entityManager.persist(lending3);

        lending4 = new Lending();
        lending4.setBook(book3);
        lending4.setReader(reader2);
        lending4.setLendingDate(LocalDate.now().minusDays(3));
        lending4.setDueDate(LocalDate.now().plusDays(11));
        lending4.setStatus(LendingStatus.ACTIVE);
        entityManager.persist(lending4);

        entityManager.flush();

        defaultPageable = PageRequest.of(0, 10);
    }


    @Test
    public void whenSaveLending_thenLendingIsSaved() {
        Lending newLending = new Lending();
        newLending.setBook(book);
        newLending.setReader(reader);
        newLending.setLendingDate(LocalDate.now());
        newLending.setDueDate(LocalDate.now().plusDays(14));
        newLending.setStatus(LendingStatus.ACTIVE);

        Lending savedLending = lendingRepository.save(newLending);

        assertThat(savedLending.getId()).isNotNull();
        assertThat(savedLending.getLendingDate()).isEqualTo(LocalDate.now());
        assertThat(savedLending.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
    }

    @Test
    public void whenFindLendingById_thenLendingIsFound() {
        Optional<Lending> foundLending = lendingRepository.findById(1L);

        assertThat(foundLending).isPresent();
        assertThat(foundLending.get().getBook().getTitle()).isEqualTo("Мастер и Маргарита");
        assertThat(foundLending.get().getReader().getLastName()).isEqualTo("Петров");
    }

    @Test
    public void whenFindLendingById_thenLendingIsNotFound() {
        Optional<Lending> foundLending = lendingRepository.findById(99L);

        assertThat(foundLending).isEmpty();
    }

    @Test
    public void whenUpdateLending_thenLendingIsUpdated() {
        Optional<Lending> lendingToUpdate = lendingRepository.findById(1L);
        assertThat(lendingToUpdate).isPresent();

        Lending lending = lendingToUpdate.get();
        lending.setReturnDate(LocalDate.now());
        lending.setStatus(LendingStatus.RETURNED);

        Lending updatedLending = lendingRepository.save(lending);

        assertThat(updatedLending.getId()).isEqualTo(1L);
        assertThat(updatedLending.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(updatedLending.getStatus()).isEqualTo(LendingStatus.RETURNED);
    }

    @Test
    public void whenDeleteLending_thenLendingIsRemoved() {
        assertThat(lendingRepository.findById(1L)).isPresent();

        lendingRepository.deleteById(1L);

        assertThat(lendingRepository.findById(1L)).isEmpty();
    }

    @Test
    public void whenFindAllLendings_thenAllLendingsAreReturned() {
        List<Lending> lendings = lendingRepository.findAll();

        assertThat(lendings).hasSize(4);
    }

    @Test
    public void whenFindByStatus_thenReturnLendingsWithThatStatus() {
        List<Lending> activeLendings = lendingRepository.findByStatus(LendingStatus.ACTIVE, defaultPageable).getContent();
        List<Lending> overdueLendings = lendingRepository.findByStatus(LendingStatus.OVERDUE, defaultPageable).getContent();
        List<Lending> returnedLendings = lendingRepository.findByStatus(LendingStatus.RETURNED, defaultPageable).getContent();

        assertThat(activeLendings).hasSize(2);
        assertThat(overdueLendings).hasSize(1);
        assertThat(returnedLendings).hasSize(1);

        assertThat(activeLendings).extracting(Lending::getStatus)
                .containsOnly(LendingStatus.ACTIVE);
    }

    @Test
    public void whenFindByStatusAndReturnDateIsNull_thenReturnUnreturnedLendingsWithThatStatus() {
        List<Lending> activeUnreturned = lendingRepository.findByStatusAndReturnDateIsNull(LendingStatus.ACTIVE, defaultPageable).getContent();
        List<Lending> overdueUnreturned = lendingRepository.findByStatusAndReturnDateIsNull(LendingStatus.OVERDUE, defaultPageable).getContent();
        List<Lending> returnedUnreturned = lendingRepository.findByStatusAndReturnDateIsNull(LendingStatus.RETURNED, defaultPageable).getContent();

        assertThat(activeUnreturned).hasSize(2);
        assertThat(overdueUnreturned).hasSize(1);
        assertThat(returnedUnreturned).isEmpty();

        for (Lending l : activeUnreturned) {
            assertThat(l.getReturnDate()).isNull();
            assertThat(l.getStatus()).isEqualTo(LendingStatus.ACTIVE);
        }
    }

    @Test
    public void whenFindByDueDateBeforeAndReturnDateIsNull_thenReturnOverdueBooks() {
        List<Lending> overdueBooks = lendingRepository.findByDueDateBeforeAndReturnDateIsNull(LocalDate.now(), defaultPageable).getContent();

        assertThat(overdueBooks).hasSize(1);
        assertThat(overdueBooks.get(0).getStatus()).isEqualTo(LendingStatus.OVERDUE);
        assertThat(overdueBooks.get(0).getDueDate()).isBefore(LocalDate.now());
        assertThat(overdueBooks.get(0).getReturnDate()).isNull();
    }

    @Test
    public void whenFindByLendingDateBetween_thenReturnLendingsInThatPeriod() {
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = LocalDate.now();
        List<Lending> recentLendings = lendingRepository.findByLendingDateBetween(startDate, endDate, defaultPageable).getContent();

        assertThat(recentLendings).hasSize(2);
        for (Lending l : recentLendings) {
            assertThat(l.getLendingDate()).isAfterOrEqualTo(startDate);
            assertThat(l.getLendingDate()).isBeforeOrEqualTo(endDate);
        }
    }

    @Test
    public void whenFindByReaderId_thenReturnLendingsForThatReader() {
        List<Lending> reader1Lendings = lendingRepository.findByReaderId(reader.getId(), defaultPageable).getContent();
        List<Lending> reader2Lendings = lendingRepository.findByReaderId(reader2.getId(), defaultPageable).getContent();

        assertThat(reader1Lendings).hasSize(2);
        assertThat(reader2Lendings).hasSize(2);

        for (Lending l : reader1Lendings) {
            assertThat(l.getReader().getId()).isEqualTo(reader.getId());
        }
    }

    @Test
    public void whenFindByReaderIdAndStatus_thenReturnLendingsForThatReaderWithThatStatus() {
        List<Lending> reader1ActiveLendings = lendingRepository.findByReaderIdAndStatus(reader.getId(), LendingStatus.ACTIVE, defaultPageable).getContent();
        List<Lending> reader1OverdueLendings = lendingRepository.findByReaderIdAndStatus(reader.getId(), LendingStatus.OVERDUE, defaultPageable).getContent();

        assertThat(reader1ActiveLendings).hasSize(1);
        assertThat(reader1OverdueLendings).hasSize(1);

        assertThat(reader1ActiveLendings.get(0).getReader().getId()).isEqualTo(reader.getId());
        assertThat(reader1ActiveLendings.get(0).getStatus()).isEqualTo(LendingStatus.ACTIVE);
    }

    @Test
    public void whenFindByBookId_thenReturnLendingsForThatBook() {
        List<Lending> book1Lendings = lendingRepository.findByBookId(book.getId(), defaultPageable).getContent();

        assertThat(book1Lendings).hasSize(2);

        for (Lending l : book1Lendings) {
            assertThat(l.getBook().getId()).isEqualTo(book.getId());
        }
    }

    @Test
    public void whenFindTopBorrowedBooks_thenReturnPopularBooks() {
        List<BookPopularityDTO> topBooks = lendingRepository.findTopBorrowedBooks(defaultPageable).getContent();

        assertThat(topBooks).isNotEmpty();

        BookPopularityDTO firstResult = topBooks.get(0);

        assertThat(firstResult.id()).isEqualTo(book.getId());
        assertThat(firstResult.lendingCount()).isEqualTo(2L);
    }

}
