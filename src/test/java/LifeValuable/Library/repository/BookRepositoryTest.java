package LifeValuable.Library.repository;

import static org.assertj.core.api.Assertions.assertThat;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import LifeValuable.Library.config.DataConfig;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringJUnitConfig(DataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class BookRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private BookRepository bookRepository;

    private Genre fantasy;
    private Genre sciFi;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Book").executeUpdate();
        entityManager.createQuery("DELETE FROM Genre").executeUpdate();

        entityManager.createNativeQuery("ALTER TABLE book ALTER COLUMN id RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE genre ALTER COLUMN id RESTART WITH 1").executeUpdate();

        fantasy = new Genre();
        fantasy.setName("Фэнтези");
        fantasy.setDescription("Магия и волшебные существа");
        entityManager.persist(fantasy);

        sciFi = new Genre();
        sciFi.setName("Научная фантастика");
        sciFi.setDescription("Основанная на научных достижениях");
        entityManager.persist(sciFi);

        Book book1 = new Book();
        book1.setTitle("Гарри Поттер и философский камень");
        book1.setAuthor("Джоан Роулинг");
        book1.setIsbn("9785389077843");
        book1.setPublicationYear(1997);
        List<Genre> genres1 = new ArrayList<>();
        genres1.add(fantasy);
        book1.setGenres(genres1);
        book1.setStock(10);

        Book book2 = new Book();
        book2.setTitle("Дюна");
        book2.setAuthor("Фрэнк Герберт");
        book2.setIsbn("9785171367060");
        book2.setPublicationYear(1965);
        List<Genre> genres2 = new ArrayList<>();
        genres2.add(sciFi);
        book2.setGenres(genres2);
        book2.setStock(5);

        entityManager.persist(book1);
        entityManager.persist(book2);
        entityManager.flush();
    }


    @Test
    public void whenSaveBook_thenBookIsSaved() {
        Book newBook = new Book();
        newBook.setTitle("Властелин колец");
        newBook.setAuthor("Дж. Р. Р. Толкин");
        newBook.setIsbn("9785170773305");
        newBook.setPublicationYear(1954);
        List<Genre> genres = new ArrayList<>();
        genres.add(fantasy);
        newBook.setGenres(genres);
        newBook.setStock(7);

        Book savedBook = bookRepository.save(newBook);

        assertThat(savedBook.getId()).isNotNull();

        Optional<Book> foundBook = bookRepository.findById(savedBook.getId());
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Властелин колец");
        assertThat(foundBook.get().getAuthor()).isEqualTo("Дж. Р. Р. Толкин");
        assertThat(foundBook.get().getGenres()).hasSize(1);
        assertThat(foundBook.get().getGenres().get(0).getName()).isEqualTo("Фэнтези");
    }

    @Test
    public void whenFindBookById_thenBookIsFound() {
        Optional<Book> foundBook = bookRepository.findById(1L);

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Гарри Поттер и философский камень");
        assertThat(foundBook.get().getAuthor()).isEqualTo("Джоан Роулинг");
    }

    @Test
    public void whenFindBookById_thenBookIsNotFound() {
        Optional<Book> foundBook = bookRepository.findById(99L);

        assertThat(foundBook).isEmpty();
    }

    @Test
    public void whenUpdateBook_thenBookIsUpdated() {
        Optional<Book> bookToUpdate = bookRepository.findById(1L);
        assertThat(bookToUpdate).isPresent();

        Book book = bookToUpdate.get();
        String originalTitle = book.getTitle();
        book.setTitle("Гарри Поттер и тайная комната");
        book.setPublicationYear(1998);

        Book updatedBook = bookRepository.save(book);

        assertThat(updatedBook.getId()).isEqualTo(1L);
        assertThat(updatedBook.getTitle()).isNotEqualTo(originalTitle);
        assertThat(updatedBook.getTitle()).isEqualTo("Гарри Поттер и тайная комната");
        assertThat(updatedBook.getPublicationYear()).isEqualTo(1998);
    }

    @Test
    public void whenDeleteBook_thenBookIsRemoved() {
        assertThat(bookRepository.findById(1L)).isPresent();

        bookRepository.deleteById(1L);

        assertThat(bookRepository.findById(1L)).isEmpty();
    }

    @Test
    public void whenFindAllBooks_thenAllBooksAreReturned() {
        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(2);
    }


    @Test
    public void whenFindBookByTitle_thenBookIsFound() {
        Optional<Book> foundBook = bookRepository.findByTitle("Гарри Поттер и философский камень");

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getAuthor()).isEqualTo("Джоан Роулинг");
    }

    @Test
    public void whenFindBooksByTitleContaining_thenBooksAreFound() {
        List<Book> books = bookRepository.findByTitleContaining("Гарри");

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).contains("Гарри");
    }

    @Test
    public void whenFindBooksByAuthor_thenBooksAreFound() {
        List<Book> books = bookRepository.findByAuthor("Джоан Роулинг");

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Гарри Поттер и философский камень");
    }

    @Test
    public void whenFindBookByIsbn_thenBookIsFound() {
        Optional<Book> foundBook = bookRepository.findByIsbn("9785389077843");

        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getTitle()).isEqualTo("Гарри Поттер и философский камень");
    }

    @Test
    public void whenFindBookByIsbn_thenBookIsNotFound() {
        Optional<Book> foundBook = bookRepository.findByIsbn("9999999999999");

        assertThat(foundBook).isEmpty();
    }

    @Test
    public void whenFindBooksByPublicationYear_thenBooksAreFound() {
        List<Book> books = bookRepository.findByPublicationYear(1997);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Гарри Поттер и философский камень");
    }

    @Test
    public void whenFindByAllGenres_thenReturnBooksWithAllSpecifiedGenres() {
        Genre fantasy = new Genre();
        fantasy.setName("Фэнтези");
        fantasy.setDescription("Магические миры");
        entityManager.persist(fantasy);

        Genre sciFi = new Genre();
        sciFi.setName("Научная фантастика");
        sciFi.setDescription("Основана на научных теориях");
        entityManager.persist(sciFi);

        Genre adventure = new Genre();
        adventure.setName("Приключения");
        adventure.setDescription("Захватывающие события");
        entityManager.persist(adventure);

        Book book1 = new Book();
        book1.setTitle("Властелин Колец");
        book1.setAuthor("Дж. Р. Р. Толкин");
        book1.setIsbn("9785170773305");
        book1.setPublicationYear(1954);
        book1.setStock(15);
        List<Genre> genres1 = new ArrayList<>();
        genres1.add(fantasy);
        genres1.add(adventure);
        book1.setGenres(genres1);
        entityManager.persist(book1);

        Book book2 = new Book();
        book2.setTitle("Марсианин");
        book2.setAuthor("Энди Вейр");
        book2.setIsbn("9785171130848");
        book2.setPublicationYear(2011);
        book2.setStock(10);
        List<Genre> genres2 = new ArrayList<>();
        genres2.add(sciFi);
        genres2.add(adventure);
        book2.setGenres(genres2);
        entityManager.persist(book2);

        Book book3 = new Book();
        book3.setTitle("Звездная пыль");
        book3.setAuthor("Нил Гейман");
        book3.setIsbn("9785699352975");
        book3.setPublicationYear(1999);
        book3.setStock(5);
        List<Genre> genres3 = new ArrayList<>();
        genres3.add(fantasy);
        genres3.add(sciFi);
        genres3.add(adventure);
        book3.setGenres(genres3);
        entityManager.persist(book3);

        entityManager.flush();

        List<Genre> searchGenres = new ArrayList<>();
        searchGenres.add(fantasy);
        searchGenres.add(sciFi);
        List<Book> result = bookRepository.findByAllGenres(searchGenres, (long) searchGenres.size());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Звездная пыль");
    }

    @Test
    public void whenFindByPublicationYearBetween_thenBooksAreFound() {
        Book book3 = new Book();
        book3.setTitle("Метро 2033");
        book3.setAuthor("Дмитрий Глуховский");
        book3.setIsbn("9785171300869");
        book3.setPublicationYear(2005);
        List<Genre> genres3 = new ArrayList<>();
        genres3.add(sciFi);
        book3.setGenres(genres3);
        book3.setStock(8);
        entityManager.persist(book3);
        entityManager.flush();

        List<Book> books = bookRepository.findByPublicationYearBetween(1990, 2010);

        assertThat(books).hasSize(2);
        assertThat(books).extracting(Book::getPublicationYear)
                .allMatch(year -> year >= 1990 && year <= 2010);
        assertThat(books).extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Гарри Поттер и философский камень", "Метро 2033");
    }

    @Test
    public void whenFindByStockGreaterThan_thenBooksAreFound() {

        List<Book> books = bookRepository.findByStockGreaterThan(7);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Гарри Поттер и философский камень");
        assertThat(books.get(0).getStock()).isGreaterThan(7);
    }

    @Test
    public void whenFindByStockLessThan_thenBooksAreFound() {

        List<Book> books = bookRepository.findByStockLessThan(7);

        assertThat(books).hasSize(1);
        assertThat(books.get(0).getTitle()).isEqualTo("Дюна");
        assertThat(books.get(0).getStock()).isLessThan(7);
    }

    @Test
    public void whenFindByGenreName_thenBooksAreFound() {

        List<Book> fantasyBooks = bookRepository.findByGenreName("Фэнтези");
        List<Book> sciFiBooks = bookRepository.findByGenreName("Научная фантастика");

        assertThat(fantasyBooks).hasSize(1);
        assertThat(fantasyBooks.get(0).getTitle()).isEqualTo("Гарри Поттер и философский камень");

        assertThat(sciFiBooks).hasSize(1);
        assertThat(sciFiBooks.get(0).getTitle()).isEqualTo("Дюна");
    }

}
