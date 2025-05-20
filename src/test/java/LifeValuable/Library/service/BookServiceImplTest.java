package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.exception.BookNotFoundException;
import LifeValuable.Library.exception.GenreNotFoundException;
import LifeValuable.Library.mapper.BookMapper;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.repository.BookRepository;
import LifeValuable.Library.repository.GenreRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GenreRepository genreRepository;

    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    private BookServiceImpl bookService;

    private Book book;
    private BookDTO bookDTO;
    private BookDetailDTO bookDetailDTO;
    private BookPopularityDTO bookPopularityDTO;
    private CreateBookDTO createBookDTO;

    private List<Genre> genresOfCreateBookDTO;

    @BeforeEach
    void setUp() {
        List<Genre> genres = new ArrayList<>();
        Genre fantasy = new Genre();
        fantasy.setName("Фэнтези");
        fantasy.setDescription("Магия и волшебные существа");
        Genre adventure = new Genre();
        adventure.setName("Приключения");
        adventure.setDescription("Различные похождения");
        genres.add(fantasy);
        genres.add(adventure);

        book = new Book();
        book.setId(1L);
        book.setTitle("Гарри Поттер и философский камень");
        book.setAuthor("Джоан Роулинг");
        book.setIsbn("9785389077843");
        book.setPublicationYear(1997);
        book.setGenres(genres);
        book.setStock(10);
        book.setLendings(new ArrayList<>());

        bookDTO = new BookDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getPublicationYear(),
                book.getStock(), book.getGenres().stream().map(Genre::getName).toList());

        bookDetailDTO = new BookDetailDTO(book.getId(),book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublicationYear(), book.getStock(), book.getStock() - book.getLendings().size(),
                book.getGenres().stream().map(Genre::getName).toList());

        bookPopularityDTO = new BookPopularityDTO(book.getId(), book.getTitle(), book.getAuthor(), book.getLendings().size());

        createBookDTO = new CreateBookDTO("Дюна", "Фрэнк Герберт", "9785171367060", 1965,
                100, List.of("Научная фантастика"));

        genresOfCreateBookDTO = new ArrayList<>();
        Genre genre = new Genre();
        genre.setName("Научная фантастика");
        genresOfCreateBookDTO.add(genre);

        bookService = new BookServiceImpl(bookRepository, bookMapper, genreRepository);
    }

    @Test
    void whenCreate_thenSaveAndReturnBookDetailDTO() {
        Book bookToSave = bookMapper.toEntity(createBookDTO);
        bookToSave.setGenres(genresOfCreateBookDTO);

        Book savedBook = bookMapper.toEntity(createBookDTO);
        savedBook.setId(2L);
        savedBook.setGenres(genresOfCreateBookDTO);

        Book expectedBook = bookMapper.toEntity(createBookDTO);
        expectedBook.setId(2L);
        expectedBook.setGenres(genresOfCreateBookDTO);

        when(genreRepository.findByNameIn(anyList())).thenReturn(genresOfCreateBookDTO);
        when(bookRepository.save(bookToSave)).thenReturn(savedBook);

        BookDetailDTO actualDTO = bookService.create(createBookDTO);
        BookDetailDTO expectedDTO = bookMapper.toDetailDto(expectedBook);

        assertThat(actualDTO).isEqualTo(expectedDTO);

        verify(bookRepository).save(argThat(book ->
                book.getGenres() != null && book.getGenres().equals(genresOfCreateBookDTO)
        ));
        verify(genreRepository).findByNameIn(anyList());
    }

    @Test
    void whenUpdate_withExistingId_thenUpdateAndReturnBookDetailDTO() {
        Long expectedId = book.getId();
        Book finalBook = bookMapper.toEntity(createBookDTO);
        finalBook.setId(expectedId);
        finalBook.setGenres(genresOfCreateBookDTO);
        finalBook.setLendings(new ArrayList<>());

        when(bookRepository.findById(expectedId)).thenReturn(Optional.of(book));
        when(genreRepository.findByNameIn(anyList())).thenReturn(genresOfCreateBookDTO);

        BookDetailDTO actualDTO = bookService.update(createBookDTO, expectedId);
        BookDetailDTO expectedDTO = bookMapper.toDetailDto(finalBook);

        assertThat(actualDTO).isEqualTo(expectedDTO);
        verify(bookRepository).findById(expectedId);
        verify(bookRepository, never()).save(any());
        verify(genreRepository).findByNameIn(createBookDTO.genreNames());
    }

    @Test
    void whenUpdate_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(bookRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.update(createBookDTO, nonExistingId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingId);

        verify(bookRepository).findById(nonExistingId);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void whenDeleteById_withExistingId_thenCallRepositoryDelete() {
        when(bookRepository.existsById(book.getId())).thenReturn(true);
        doNothing().when(bookRepository).deleteById(book.getId());

        bookService.deleteById(book.getId());

        verify(bookRepository).existsById(book.getId());
        verify(bookRepository).deleteById(book.getId());
    }

    @Test
    void whenDeleteById_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(bookRepository.existsById(nonExistingId)).thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteById(nonExistingId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingId);

        verify(bookRepository).existsById(nonExistingId);
        verify(bookRepository, never()).deleteById(nonExistingId);
    }

    @Test
    void whenFindById_withExistingId_thenReturnBookDetailDTO() {
        Long id = book.getId();
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        BookDetailDTO actualDTO = bookService.findById(id);

        assertThat(actualDTO).isEqualTo(bookDetailDTO);
        verify(bookRepository).findById(id);
    }

    @Test
    void whenFindById_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(bookRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.findById(nonExistingId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingId);

        verify(bookRepository).findById(nonExistingId);
    }

    @Test
    void whenFindAll_thenReturnPageOfBookDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(book.getId());
        assertThat(result.getContent().get(0).title()).isEqualTo(book.getTitle());
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void whenFindAll_thenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getTotalElements()).isZero();
        verify(bookRepository).findAll(pageable);
    }

    @Test
    void whenFindByTitle_thenReturnBookDetailDTO() {
        String title = "Гарри Поттер и философский камень";
        when(bookRepository.findByTitle(title)).thenReturn(Optional.of(book));

        Optional<BookDetailDTO> result = bookService.findByTitle(title);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(bookDetailDTO);
        verify(bookRepository).findByTitle(title);
    }

    @Test
    void whenFindByTitle_thenReturnOptionalEmpty() {
        String nonExistingTitle = "Несуществующая книга";
        when(bookRepository.findByTitle(nonExistingTitle)).thenReturn(Optional.empty());

        Optional<BookDetailDTO> result = bookService.findByTitle(nonExistingTitle);

        assertThat(result).isEmpty();
        verify(bookRepository).findByTitle(nonExistingTitle);
    }

    @Test
    void whenFindByTitleContaining_withExistingFragment_thenReturnPageOfBooks() {
        String fragment = "Поттер";
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByTitleContaining(fragment, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByTitleContaining(fragment, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).contains(fragment);
        verify(bookRepository).findByTitleContaining(fragment, pageable);
    }

    @Test
    void whenFindByTitleContaining_withNonExistingFragment_thenReturnEmptyPage() {
        String fragment = "Непосидящий";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByTitleContaining(fragment, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByTitleContaining(fragment, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByTitleContaining(fragment, pageable);
    }

    @Test
    void whenFindByAuthor_withExistingAuthor_thenReturnPageOfBooks() {
        String author = "Джоан Роулинг";
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByAuthor(author, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByAuthor(author, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).author()).isEqualTo(author);
        verify(bookRepository).findByAuthor(author, pageable);
    }

    @Test
    void whenFindByAuthor_withNonExistingAuthor_thenReturnEmptyPage() {
        String author = "Неизвестный Автор";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByAuthor(author, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByAuthor(author, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByAuthor(author, pageable);
    }

    @Test
    void whenFindByIsbn_withExistingIsbn_thenReturnBookDetailDTO() {
        String isbn = "9785389077843";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        Optional<BookDetailDTO> result = bookService.findByIsbn(isbn);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(bookDetailDTO);
        verify(bookRepository).findByIsbn(isbn);
    }

    @Test
    void whenFindByIsbn_withNonExistingIsbn_thenReturnEmptyOptional() {
        String isbn = "9999999999999";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        Optional<BookDetailDTO> result = bookService.findByIsbn(isbn);

        assertThat(result).isEmpty();
        verify(bookRepository).findByIsbn(isbn);
    }

    @Test
    void whenFindByIsbn_withInvalidIsbn_thenReturnEmptyOptional() {
        String invalidIsbn = "invalid-isbn";
        when(bookRepository.findByIsbn(invalidIsbn)).thenReturn(Optional.empty());

        Optional<BookDetailDTO> result = bookService.findByIsbn(invalidIsbn);

        assertThat(result).isEmpty();
        verify(bookRepository).findByIsbn(invalidIsbn);
    }

    @Test
    void whenFindByPublicationYear_withExistingYear_thenReturnPageOfBooks() {
        Integer year = 1997;
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByPublicationYear(year, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByPublicationYear(year, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).publicationYear()).isEqualTo(year);
        verify(bookRepository).findByPublicationYear(year, pageable);
    }

    @Test
    void whenFindByPublicationYear_withNonExistingYear_thenReturnEmptyPage() {
        Integer year = 2050;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByPublicationYear(year, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByPublicationYear(year, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByPublicationYear(year, pageable);
    }

    @Test
    void whenFindByPublicationYearBetween_withValidRange_thenReturnPageOfBooks() {
        Integer startYear = 1990;
        Integer endYear = 2000;
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByPublicationYearBetween(startYear, endYear, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByPublicationYearBetween(startYear, endYear, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).publicationYear()).isBetween(startYear, endYear);
        verify(bookRepository).findByPublicationYearBetween(startYear, endYear, pageable);
    }

    @Test
    void whenFindByPublicationYearBetween_withNoMatchingBooks_thenReturnEmptyPage() {
        Integer startYear = 2020;
        Integer endYear = 2030;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByPublicationYearBetween(startYear, endYear, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByPublicationYearBetween(startYear, endYear, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByPublicationYearBetween(startYear, endYear, pageable);
    }

    @Test
    void whenFindByPublicationYearBetween_withInvalidRange_thenThrowException() {
        Integer startYear = 2000;
        Integer endYear = 1990;

        assertThatThrownBy(() -> bookService.findByPublicationYearBetween(startYear, endYear, PageRequest.of(0, 10)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Start year must be less than or equal to end year");

        verify(bookRepository, never()).findByPublicationYearBetween(any(), any(), any());
    }

    @Test
    void whenFindByGenreName_withExistingGenre_thenReturnPageOfBooks() {
        String genreName = "Фэнтези";
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByGenreName(genreName, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByGenreName(genreName, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).genreNames()).contains("Фэнтези", "Приключения");
        verify(bookRepository).findByGenreName(genreName, pageable);
    }

    @Test
    void whenFindByGenreName_withNonExistingGenre_thenReturnEmptyPage() {
        String genreName = "Несуществующий жанр";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByGenreName(genreName, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByGenreName(genreName, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByGenreName(genreName, pageable);
    }

    @Test
    void whenFindByAllGenres_withMatchingBooks_thenReturnPageOfBooks() {
        List<String> genreNames = List.of("Фэнтези", "Приключения");
        Pageable pageable = PageRequest.of(0, 10);
        List<Genre> genres = genreNames.stream().map(name -> {
            Genre genre = new Genre();
            genre.setName(name);
            return genre;
        }).toList();
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(genreRepository.findByNameIn(genreNames)).thenReturn(genres);
        when(bookRepository.findByAllGenres(eq(genres), eq(pageable))).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByAllGenres(genreNames, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(genreRepository).findByNameIn(genreNames);
        verify(bookRepository).findByAllGenres(eq(genres), eq(pageable));
    }

    @Test
    void whenFindByAllGenres_withNoMatchingBooks_thenReturnEmptyPage() {
        List<String> genreNames = List.of("Детектив", "Триллер");
        Pageable pageable = PageRequest.of(0, 10);
        List<Genre> genres = genreNames.stream().map(name -> {
            Genre genre = new Genre();
            genre.setName(name);
            return genre;
        }).toList();
        Page<Book> emptyPage = Page.empty(pageable);

        when(genreRepository.findByNameIn(genreNames)).thenReturn(genres);
        when(bookRepository.findByAllGenres(eq(genres), eq(pageable))).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByAllGenres(genreNames, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(genreRepository).findByNameIn(genreNames);
        verify(bookRepository).findByAllGenres(eq(genres), eq(pageable));
    }

    @Test
    void whenFindByAllGenres_withEmptyGenresList_thenThrowException() {
        List<String> emptyGenresList = List.of();
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> bookService.findByAllGenres(emptyGenresList, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Genre names list cannot be empty");

        verify(genreRepository, never()).findByNameIn(anyList());
        verify(bookRepository, never()).findByAllGenres(anyList(), any());
    }

    @Test
    void whenAddGenreToBook_withExistingBookAndGenre_thenReturnUpdatedBook() {
        Long bookId = book.getId();
        String genreName = "Научная фантастика";

        List<Genre> originalGenres = new ArrayList<>(book.getGenres());

        Genre newGenre = new Genre();
        newGenre.setName(genreName);
        newGenre.setDescription("Описание научной фантастики");

        List<Genre> updatedGenres = new ArrayList<>(originalGenres);
        updatedGenres.add(newGenre);

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle(book.getTitle());
        updatedBook.setAuthor(book.getAuthor());
        updatedBook.setIsbn(book.getIsbn());
        updatedBook.setPublicationYear(book.getPublicationYear());
        updatedBook.setStock(book.getStock());
        updatedBook.setGenres(updatedGenres);
        updatedBook.setLendings(book.getLendings());

        BookDetailDTO expectedDto = bookMapper.toDetailDto(updatedBook);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(genreRepository.findByName(genreName)).thenReturn(Optional.of(newGenre));

        BookDetailDTO result = bookService.addGenreToBook(bookId, genreName);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getGenres()).contains(newGenre);
        verify(bookRepository).findById(bookId);
        verify(genreRepository).findByName(genreName);
    }

    @Test
    void whenAddGenreToBook_withNonExistingBook_thenThrowBookNotFoundException() {
        Long nonExistingBookId = 99L;
        String genreName = "Научная фантастика";

        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.addGenreToBook(nonExistingBookId, genreName))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingBookId);

        verify(bookRepository).findById(nonExistingBookId);
        verify(genreRepository, never()).findByName(anyString());
    }

    @Test
    void whenAddGenreToBook_withNonExistingGenre_thenThrowException() {
        Long bookId = book.getId();
        String nonExistingGenreName = "Несуществующий жанр";

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(genreRepository.findByName(nonExistingGenreName)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.addGenreToBook(bookId, nonExistingGenreName))
                .isInstanceOf(GenreNotFoundException.class)
                .hasMessageContaining("Genre not found with name: " + nonExistingGenreName);

        verify(bookRepository).findById(bookId);
        verify(genreRepository).findByName(nonExistingGenreName);
    }

    @Test
    void whenAddGenreToBook_withGenreAlreadyAdded_thenReturnUnchangedBook() {
        Long bookId = book.getId();
        String existingGenreName = "Фэнтези";

        Genre existingGenre = book.getGenres().get(0);

        BookDetailDTO expectedDto = bookMapper.toDetailDto(book);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDetailDTO result = bookService.addGenreToBook(bookId, existingGenreName);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getGenres().size()).isEqualTo(2);
        verify(bookRepository).findById(bookId);
        verify(genreRepository, never()).findByName(existingGenreName);
    }

    @Test
    void whenRemoveGenreFromBook_withExistingBookAndGenre_thenReturnUpdatedBook() {
        Long bookId = book.getId();
        String genreName = "Фэнтези";

        Genre genreToRemove = book.getGenres().get(0);

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle(book.getTitle());
        updatedBook.setAuthor(book.getAuthor());
        updatedBook.setIsbn(book.getIsbn());
        updatedBook.setPublicationYear(book.getPublicationYear());
        updatedBook.setStock(book.getStock());
        List<Genre> newGenres = new ArrayList<>();
        newGenres.add(book.getGenres().get(1));
        updatedBook.setGenres(newGenres);
        updatedBook.setLendings(book.getLendings());

        BookDetailDTO expectedDto = bookMapper.toDetailDto(updatedBook);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDetailDTO result = bookService.removeGenreFromBook(bookId, genreName);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getGenres()).doesNotContain(genreToRemove);
        verify(bookRepository).findById(bookId);
        verify(genreRepository, never()).findByName(genreName);
    }

    @Test
    void whenRemoveGenreFromBook_withNonExistingBook_thenThrowBookNotFoundException() {
        Long nonExistingBookId = 99L;
        String genreName = "Фэнтези";

        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.removeGenreFromBook(nonExistingBookId, genreName))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingBookId);

        verify(bookRepository).findById(nonExistingBookId);
        verify(genreRepository, never()).findByName(anyString());
    }

    @Test
    void whenRemoveGenreFromBook_withNonExistingGenreInBook_thenThrowException() {
        Long bookId = book.getId();
        String nonExistingGenreName = "Детектив";

        Genre nonExistingGenre = new Genre();
        nonExistingGenre.setName(nonExistingGenreName);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.removeGenreFromBook(bookId, nonExistingGenreName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book does not have genre: " + nonExistingGenreName);

        verify(bookRepository).findById(bookId);
        verify(genreRepository, never()).findByName(nonExistingGenreName);
    }

    @Test
    void whenRemoveGenreFromBook_withLastGenre_thenThrowException() {
        Long bookId = book.getId();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        bookService.removeGenreFromBook(bookId, "Приключения");
        String lastGenreName = "Фэнтези";
        assertThatThrownBy(() -> bookService.removeGenreFromBook(bookId, lastGenreName))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Can't remove the last genre from a book");

        verify(bookRepository, times(2)).findById(bookId);
        verify(genreRepository, never()).findByName(lastGenreName);
    }

    @Test
    void whenFindByStockAvailable_thenReturnPageOfAvailableBooks() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByStockGreaterThan(0, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByStockAvailable(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).stock()).isGreaterThan(0);
        verify(bookRepository).findByStockGreaterThan(0, pageable);
    }

    @Test
    void whenFindByStockAvailable_withNoAvailableBooks_thenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByStockGreaterThan(0, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByStockAvailable(pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByStockGreaterThan(0, pageable);
    }

    @Test
    void whenFindByStockGreaterThan_withMatchingBooks_thenReturnPageOfBooks() {
        Integer minStock = 5;
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByStockGreaterThan(minStock, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByStockGreaterThan(minStock, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).stock()).isGreaterThan(minStock);
        verify(bookRepository).findByStockGreaterThan(minStock, pageable);
    }

    @Test
    void whenFindByStockGreaterThan_withNoMatchingBooks_thenReturnEmptyPage() {
        Integer minStock = 100;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByStockGreaterThan(minStock, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByStockGreaterThan(minStock, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByStockGreaterThan(minStock, pageable);
    }

    @Test
    void whenFindByStockLessThan_withMatchingBooks_thenReturnPageOfBooks() {
        Integer maxStock = 15;
        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findByStockLessThan(maxStock, pageable)).thenReturn(bookPage);

        Page<BookDTO> result = bookService.findByStockLessThan(maxStock, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).stock()).isLessThan(maxStock);
        verify(bookRepository).findByStockLessThan(maxStock, pageable);
    }

    @Test
    void whenFindByStockLessThan_withNoMatchingBooks_thenReturnEmptyPage() {
        Integer maxStock = 5;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = Page.empty(pageable);

        when(bookRepository.findByStockLessThan(maxStock, pageable)).thenReturn(emptyPage);

        Page<BookDTO> result = bookService.findByStockLessThan(maxStock, pageable);

        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        verify(bookRepository).findByStockLessThan(maxStock, pageable);
    }

    @Test
    void whenUpdateBookStock_withExistingBook_thenUpdateStockAndReturnBook() {
        Long bookId = book.getId();
        Integer newStock = 20;

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle(book.getTitle());
        updatedBook.setAuthor(book.getAuthor());
        updatedBook.setIsbn(book.getIsbn());
        updatedBook.setPublicationYear(book.getPublicationYear());
        updatedBook.setStock(newStock);
        updatedBook.setGenres(book.getGenres());
        updatedBook.setLendings(book.getLendings());

        BookDetailDTO expectedDto = bookMapper.toDetailDto(updatedBook);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDetailDTO result = bookService.updateBookStock(bookId, newStock);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getStock()).isEqualTo(newStock);
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenUpdateBookStock_withNonExistingBook_thenThrowBookNotFoundException() {
        Long nonExistingBookId = 99L;
        Integer newStock = 20;

        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBookStock(nonExistingBookId, newStock))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingBookId);

        verify(bookRepository).findById(nonExistingBookId);
    }

    @Test
    void whenUpdateBookStock_withNegativeStock_thenThrowException() {
        Long bookId = book.getId();
        Integer negativeStock = -5;

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        assertThatThrownBy(() -> bookService.updateBookStock(bookId, negativeStock))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Can't set book stock less than zero");

        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenUpdateBookStock_withZeroStock_thenUpdateSuccessfully() {
        Long bookId = book.getId();
        Integer zeroStock = 0;

        Book updatedBook = new Book();
        updatedBook.setId(bookId);
        updatedBook.setTitle(book.getTitle());
        updatedBook.setAuthor(book.getAuthor());
        updatedBook.setIsbn(book.getIsbn());
        updatedBook.setPublicationYear(book.getPublicationYear());
        updatedBook.setStock(zeroStock);
        updatedBook.setGenres(book.getGenres());
        updatedBook.setLendings(book.getLendings());

        BookDetailDTO expectedDto = bookMapper.toDetailDto(updatedBook);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        BookDetailDTO result = bookService.updateBookStock(bookId, zeroStock);

        assertThat(result).isEqualTo(expectedDto);
        assertThat(book.getStock()).isEqualTo(zeroStock);
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenIsAvailableForLending_withBookHavingAvailableStock_thenReturnTrue() {
        Long bookId = book.getId();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        boolean result = bookService.isAvailableForLending(bookId);

        assertThat(result).isTrue();
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenIsAvailableForLending_withBookHavingNoAvailableStock_thenReturnFalse() {
        Long bookId = book.getId();
        book.setStock(0);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        boolean result = bookService.isAvailableForLending(bookId);

        assertThat(result).isFalse();
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenIsAvailableForLending_withNonExistingBook_thenThrowBookNotFoundException() {
        Long nonExistingBookId = 99L;

        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.isAvailableForLending(nonExistingBookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingBookId);

        verify(bookRepository).findById(nonExistingBookId);
    }

    @Test
    void whenGetAvailableStockCount_withExistingBook_thenReturnCorrectCount() {
        Long bookId = book.getId();
        int expectedAvailableStock = book.getStock() - book.getLendings().size();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        int result = bookService.getAvailableStockCount(bookId);

        assertThat(result).isEqualTo(expectedAvailableStock);
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenGetAvailableStockCount_withBookWithNoBorrows_thenReturnFullStock() {
        Long bookId = book.getId();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        int result = bookService.getAvailableStockCount(bookId);

        assertThat(result).isEqualTo(book.getStock());
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenGetAvailableStockCount_withAllBooksBorrowed_thenReturnZero() {
        Long bookId = book.getId();
        List<Lending> activeLendings = new ArrayList<>();
        for (int i = 0; i < book.getStock(); i++) {
            Lending lending = new Lending();
            lending.setStatus(LendingStatus.ACTIVE);
            activeLendings.add(lending);
        }
        book.setLendings(activeLendings);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        int result = bookService.getAvailableStockCount(bookId);

        assertThat(result).isZero();
        verify(bookRepository).findById(bookId);
    }

    @Test
    void whenGetAvailableStockCount_withNonExistingBook_thenThrowBookNotFoundException() {
        Long nonExistingBookId = 99L;

        when(bookRepository.findById(nonExistingBookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getAvailableStockCount(nonExistingBookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + nonExistingBookId);

        verify(bookRepository).findById(nonExistingBookId);
    }
}
