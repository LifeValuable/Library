package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.exception.BookNotFoundException;
import LifeValuable.Library.exception.GlobalExceptionHandler;
import LifeValuable.Library.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {
    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController controller;

    private CreateBookDTO createBookDTO;
    private BookDetailDTO bookDetailDTO;
    private BookDTO bookDTO;
    private ObjectMapper objectMapper;

    private final Sort defaultSort = Sort.by("title").ascending();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();

        createBookDTO = new CreateBookDTO(
                "Дюна", "Фрэнк Герберт", "9785171367060", 1965, 100,
                List.of("Научная фантастика")
        );

        bookDetailDTO = new BookDetailDTO(
                1L, "Дюна", "Фрэнк Герберт", "9785171367060", 1965, 100, 100,
                List.of("Научная фантастика")
        );

        bookDTO = new BookDTO(
                1L, "Дюна", "Фрэнк Герберт", 1965, 100, List.of("Научная фантастика")
        );
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }


    @Test
    void whenGetAllBooks_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findAll(pageable)).thenReturn(booksPage);

        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Дюна"))
                .andExpect(jsonPath("$.content[0].author").value("Фрэнк Герберт"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(bookService).findAll(pageable);
    }

    @Test
    void whenGetBookById_thenReturnBookDetails() throws Exception {
        when(bookService.findById(1L)).thenReturn(bookDetailDTO);

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Дюна"))
                .andExpect(jsonPath("$.author").value("Фрэнк Герберт"))
                .andExpect(jsonPath("$.isbn").value("9785171367060"));

        verify(bookService).findById(1L);
    }

    @Test
    void whenGetBookById_thenBookNotFound() throws Exception {
        when(bookService.findById(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена книга с id: 99"));

        verify(bookService).findById(99L);
    }

    @Test
    void whenCreateBook_thenReturnBookDetails() throws Exception {
        when(bookService.create(any(CreateBookDTO.class))).thenReturn(bookDetailDTO);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createBookDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/books/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Дюна"));

        verify(bookService).create(any(CreateBookDTO.class));
    }

    @Test
    void whenCreateBookWithInvalidData_thenReturnValidationErrors() throws Exception {
        CreateBookDTO invalidBook = new CreateBookDTO("", "", "", -1, -1, List.of());

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("title", "author", "isbn", "publicationYear", "stock")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(bookService, never()).create(any(CreateBookDTO.class));
    }

    @Test
    void whenUpdateBook_thenReturnUpdatedBookDetails() throws Exception {
        when(bookService.update(any(CreateBookDTO.class), eq(1L))).thenReturn(bookDetailDTO);

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createBookDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Дюна"));

        verify(bookService).update(any(CreateBookDTO.class), eq(1L));
    }

    @Test
    void whenUpdateNonExistentBook_thenReturnNotFound() throws Exception {
        when(bookService.update(any(CreateBookDTO.class), eq(99L)))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(put("/api/books/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createBookDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена книга с id: 99"));

        verify(bookService).update(any(CreateBookDTO.class), eq(99L));
    }

    @Test
    void whenUpdateBookWithInvalidData_thenReturnValidationErrors() throws Exception {
        CreateBookDTO invalidBook = new CreateBookDTO("", "", "", -1, -1, List.of());

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("title", "author", "isbn", "publicationYear", "stock")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(bookService, never()).update(any(CreateBookDTO.class), anyLong());
    }

    @Test
    void whenDeleteBook_thenReturnNoContent() throws Exception {
        doNothing().when(bookService).deleteById(1L);

        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

    @Test
    void whenDeleteNonExistentBook_thenReturnNotFound() throws Exception {
        doThrow(new BookNotFoundException(99L)).when(bookService).deleteById(99L);

        mockMvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена книга с id: 99"));

        verify(bookService).deleteById(99L);
    }

    @Test
    void whenSearchWithoutParameters_thenReturnAllBooks() throws Exception {
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), PageRequest.of(0, 20, Sort.by("title").ascending()), 1);
        when(bookService.findAll(any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Дюна"));

        verify(bookService).findAll(any(Pageable.class));
        verify(bookService, never()).findByTitle(anyString());
    }


    @Test
    void whenSearchByExactTitle_thenReturnSingleBook() throws Exception {
        Optional<BookDetailDTO> bookOptional = Optional.of(bookDetailDTO);
        when(bookService.findByTitle("Дюна")).thenReturn(bookOptional);

        mockMvc.perform(get("/api/books/search")
                        .param("title", "Дюна"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Дюна"))
                .andExpect(jsonPath("$.author").value("Фрэнк Герберт"));

        verify(bookService).findByTitle("Дюна");
    }

    @Test
    void whenSearchByNonExistentTitle_thenReturnNotFound() throws Exception {
        when(bookService.findByTitle("Несуществующая книга")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/search")
                        .param("title", "Несуществующая книга"))
                .andExpect(status().isNotFound());

        verify(bookService).findByTitle("Несуществующая книга");
    }

    @Test
    void whenSearchByExactIsbn_thenReturnSingleBook() throws Exception {
        Optional<BookDetailDTO> bookOptional = Optional.of(bookDetailDTO);
        when(bookService.findByIsbn("9785171367060")).thenReturn(bookOptional);

        mockMvc.perform(get("/api/books/search")
                        .param("isbn", "9785171367060"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("9785171367060"));

        verify(bookService).findByIsbn("9785171367060");
    }

    @Test
    void whenSearchByNonExistentIsbn_thenReturnNotFound() throws Exception {
        when(bookService.findByIsbn("9999999999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/books/search")
                        .param("isbn", "9999999999999"))
                .andExpect(status().isNotFound());

        verify(bookService).findByIsbn("9999999999999");
    }

    @Test
    void whenSearchByTitleFragment_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findByTitleContaining(eq("Дюн"), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("titleFragment", "Дюн")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Дюна"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(bookService).findByTitleContaining(eq("Дюн"), any(Pageable.class));
    }

    @Test
    void whenSearchByAuthor_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findByAuthor(eq("Фрэнк Герберт"), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("author", "Фрэнк Герберт")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].author").value("Фрэнк Герберт"));

        verify(bookService).findByAuthor(eq("Фрэнк Герберт"), any(Pageable.class));
    }

    @Test
    void whenSearchByPublicationYear_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findByPublicationYear(eq(1965), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("publicationYear", "1965")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].publicationYear").value(1965));

        verify(bookService).findByPublicationYear(eq(1965), any(Pageable.class));
    }

    @Test
    void whenSearchByYearRange_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findByPublicationYearBetween(eq(1960), eq(1970), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("startYear", "1960")
                        .param("endYear", "1970")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].publicationYear").value(1965));

        verify(bookService).findByPublicationYearBetween(eq(1960), eq(1970), any(Pageable.class));
    }

    @Test
    void whenSearchByGenre_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findByGenreName(eq("Научная фантастика"), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("genre", "Научная фантастика")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].genreNames[0]").value("Научная фантастика"));

        verify(bookService).findByGenreName(eq("Научная фантастика"), any(Pageable.class));
    }

    @Test
    void whenSearchByMultipleGenres_thenReturnPageOfBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        List<String> genres = List.of("Научная фантастика", "Приключения");
        when(bookService.findByAllGenres(eq(genres), any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("genres", "Научная фантастика", "Приключения")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(bookService).findByAllGenres(eq(genres), any(Pageable.class));
    }

    @Test
    void whenSearchByAuthorReturnsEmptyPage_thenReturnEmptyPage() throws Exception {
        Page<BookDTO> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20, defaultSort), 0);
        when(bookService.findByAuthor(eq("Несуществующий автор"), any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/books/search")
                        .param("author", "Несуществующий автор")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));

        verify(bookService).findByAuthor(eq("Несуществующий автор"), any(Pageable.class));
    }

    @Test
    void whenSearchWithOnlyStartYear_thenReturnAllBooks() throws Exception {
        Pageable pageable = PageRequest.of(0, 20, defaultSort);
        Page<BookDTO> booksPage = new PageImpl<>(List.of(bookDTO), pageable, 1);
        when(bookService.findAll(any(Pageable.class))).thenReturn(booksPage);

        mockMvc.perform(get("/api/books/search")
                        .param("startYear", "1960")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(bookService).findAll(any(Pageable.class));
        verify(bookService, never()).findByPublicationYearBetween(anyInt(), anyInt(), any(Pageable.class));
    }

    @Test
    void whenUpdateBookStock_thenReturnUpdatedBook() throws Exception {
        BookDetailDTO updatedBook = new BookDetailDTO(
                1L, "Дюна", "Фрэнк Герберт", "9785171367060", 1965, 50, 50,
                List.of("Научная фантастика")
        );
        when(bookService.updateBookStock(1L, 50)).thenReturn(updatedBook);

        mockMvc.perform(patch("/api/books/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": 50}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(50))
                .andExpect(jsonPath("$.availableStock").value(50));

        verify(bookService).updateBookStock(1L, 50);
    }

    @Test
    void whenUpdateBookStockWithNegativeValue_thenReturnValidationErrors() throws Exception {
        mockMvc.perform(patch("/api/books/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stock\": -5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field", hasItem("stock")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(bookService, never()).updateBookStock(anyLong(), anyInt());
    }

    @Test
    void whenAddGenreToBook_thenReturnUpdatedBook() throws Exception {
        BookDetailDTO updatedBook = new BookDetailDTO(
                1L, "Дюна", "Фрэнк Герберт", "9785171367060", 1965, 100, 100,
                List.of("Научная фантастика", "Приключения")
        );
        when(bookService.addGenreToBook(1L, "Приключения")).thenReturn(updatedBook);

        mockMvc.perform(post("/api/books/1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genreName\": \"Приключения\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genreNames", hasSize(2)))
                .andExpect(jsonPath("$.genreNames", hasItems("Научная фантастика", "Приключения")));

        verify(bookService).addGenreToBook(1L, "Приключения");
    }

    @Test
    void whenAddGenreToNonExistentBook_thenReturnNotFound() throws Exception {
        when(bookService.addGenreToBook(99L, "Приключения"))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(post("/api/books/99/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genreName\": \"Приключения\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена книга с id: 99"));

        verify(bookService).addGenreToBook(99L, "Приключения");
    }

    @Test
    void whenAddGenreWithBlankName_thenReturnValidationErrors() throws Exception {
        mockMvc.perform(post("/api/books/1/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"genreName\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field", hasItem("genreName")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(bookService, never()).addGenreToBook(anyLong(), anyString());
    }

    @Test
    void whenRemoveGenreFromBook_thenReturnUpdatedBook() throws Exception {
        BookDetailDTO updatedBook = new BookDetailDTO(
                1L, "Дюна", "Фрэнк Герберт", "9785171367060", 1965, 100, 100,
                List.of()
        );
        when(bookService.removeGenreFromBook(1L, "Научная фантастика")).thenReturn(updatedBook);

        mockMvc.perform(delete("/api/books/{id}/genres/{genreName}", 1L, "Научная фантастика"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.genreNames", hasSize(0)));

        verify(bookService).removeGenreFromBook(1L, "Научная фантастика");
    }

    @Test
    void whenRemoveGenreFromNonExistentBook_thenReturnNotFound() throws Exception {
        when(bookService.removeGenreFromBook(99L, "Научная фантастика"))
                .thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(delete("/api/books/{id}/genres/{genreName}", 99L, "Научная фантастика"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена книга с id: 99"));

        verify(bookService).removeGenreFromBook(99L, "Научная фантастика");
    }
}
