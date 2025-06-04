package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;

@Tag(name = "Книги", description = "Управление библиотечными книгами")
@Validated
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    public record StockUpdateRequest(@Min(0) @NotNull Integer stock) {}
    public record GenreRequest(@NotBlank String genreName) {}

    @Operation(summary = "Получить все книги", description = "Возвращает пагинированный список всех книг с возможностью сортировки")
    @ApiResponse(responseCode = "200", description = "Список книг успешно получен")
    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @Parameter(description = "Параметры пагинации и сортировки")
            @PageableDefault(sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }

    @Operation(summary = "Получить книгу по ID", description = "Возвращает подробную информацию о книге по её идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книга найдена"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> getById(
            @Parameter(description = "Уникальный идентификатор книги", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @Operation(summary = "Создать новую книгу", description = "Добавляет новую книгу в библиотеку")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Книга успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные книги")
    })
    @PostMapping
    public ResponseEntity<BookDetailDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой книги", required = true)
            @Valid @RequestBody CreateBookDTO bookDTO) {
        BookDetailDTO created = bookService.create(bookDTO);
        URI location = URI.create("/api/books/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Удалить книгу", description = "Удаляет книгу из библиотеки по её идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Книга успешно удалена"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Идентификатор книги для удаления")
            @PathVariable Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить книгу", description = "Обновляет информацию о книге по её идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книга успешно обновлена"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDetailDTO> update(
            @Parameter(description = "Идентификатор книги") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные книги", required = true)
            @Valid @RequestBody CreateBookDTO bookDTO) {
        return ResponseEntity.ok(bookService.update(bookDTO, id));
    }

    @Operation(summary = "Поиск по точному названию", description = "Находит книгу по точному совпадению названия")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книга найдена"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @GetMapping(value = "/search", params = {"title", "!titleFragment"})
    public ResponseEntity<?> searchByTitle(
            @Parameter(description = "Точное название книги", example = "Война и мир")
            @RequestParam String title) {
        return bookService.findByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Поиск по ISBN", description = "Находит книгу по её ISBN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книга найдена"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @GetMapping(value = "/search", params = "isbn")
    public ResponseEntity<?> searchByIsbn(
            @Parameter(description = "ISBN книги", example = "978-5-17-085357-2")
            @RequestParam String isbn) {
        return bookService.findByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Поиск по фрагменту названия", description = "Находит книги, содержащие указанный фрагмент в названии")
    @ApiResponse(responseCode = "200", description = "Список книг найден")
    @GetMapping(value = "/search", params = {"titleFragment", "!title"})
    public ResponseEntity<Page<BookDTO>> searchByTitleFragment(
            @Parameter(description = "Фрагмент названия книги", example = "война")
            @RequestParam String titleFragment,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByTitleContaining(titleFragment, pageable));
    }

    @Operation(summary = "Поиск по автору", description = "Находит все книги указанного автора")
    @ApiResponse(responseCode = "200", description = "Список книг автора")
    @GetMapping(value = "/search", params = "author")
    public ResponseEntity<Page<BookDTO>> searchByAuthor(
            @Parameter(description = "Имя автора", example = "Лев Толстой")
            @RequestParam String author,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAuthor(author, pageable));
    }

    @Operation(summary = "Поиск по году публикации", description = "Находит книги, изданные в указанном году")
    @ApiResponse(responseCode = "200", description = "Список книг за указанный год")
    @GetMapping(value = "/search", params = "publicationYear")
    public ResponseEntity<Page<BookDTO>> searchByPublicationYear(
            @Parameter(description = "Год публикации", example = "2020")
            @RequestParam Integer publicationYear,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByPublicationYear(publicationYear, pageable));
    }

    @Operation(summary = "Поиск по диапазону лет", description = "Находит книги, изданные в указанном диапазоне лет")
    @ApiResponse(responseCode = "200", description = "Список книг за диапазон лет")
    @GetMapping(value = "/search", params = {"startYear", "endYear"})
    public ResponseEntity<Page<BookDTO>> searchByYearRange(
            @Parameter(description = "Начальный год", example = "2000") @RequestParam Integer startYear,
            @Parameter(description = "Конечный год", example = "2020") @RequestParam Integer endYear,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByPublicationYearBetween(startYear, endYear, pageable));
    }

    @Operation(summary = "Поиск по жанру", description = "Находит все книги указанного жанра")
    @ApiResponse(responseCode = "200", description = "Список книг жанра")
    @GetMapping(value = "/search", params = "genre")
    public ResponseEntity<Page<BookDTO>> searchByGenre(
            @Parameter(description = "Название жанра", example = "Фантастика")
            @RequestParam String genre,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByGenreName(genre, pageable));
    }

    @Operation(summary = "Поиск по нескольким жанрам", description = "Находит книги, относящиеся ко всем указанным жанрам")
    @ApiResponse(responseCode = "200", description = "Список книг с указанными жанрами")
    @GetMapping(value = "/search", params = "genres")
    public ResponseEntity<Page<BookDTO>> searchByGenres(
            @Parameter(description = "Список жанров", example = "[\"Фантастика\", \"Приключения\"]")
            @RequestParam List<String> genres,
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAllGenres(genres, pageable));
    }

    @Operation(summary = "Получить все книги (поиск без фильтров)", description = "Возвращает все книги без применения фильтров")
    @ApiResponse(responseCode = "200", description = "Список всех книг")
    @GetMapping(value = "/search")
    public ResponseEntity<Page<BookDTO>> searchAll(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }

    @Operation(summary = "Обновить количество на складе", description = "Изменяет количество доступных экземпляров книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Количество успешно обновлено"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректное количество")
    })
    @PatchMapping("/{id}/stock")
    public ResponseEntity<BookDetailDTO> updateStock(
            @Parameter(description = "Идентификатор книги") @PathVariable @Min(1) Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новое количество экземпляров", required = true)
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(bookService.updateBookStock(id, request.stock()));
    }

    @Operation(summary = "Добавить жанр к книге", description = "Добавляет новый жанр к существующей книге")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Жанр успешно добавлен"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректное название жанра")
    })
    @PostMapping("/{id}/genres")
    public ResponseEntity<BookDetailDTO> addGenre(
            @Parameter(description = "Идентификатор книги") @PathVariable @Min(1) Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Название жанра для добавления", required = true)
            @Valid @RequestBody GenreRequest request) {
        return ResponseEntity.ok(bookService.addGenreToBook(id, request.genreName()));
    }

    @Operation(summary = "Удалить жанр из книги", description = "Удаляет указанный жанр из книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Жанр успешно удален"),
            @ApiResponse(responseCode = "404", description = "Книга или жанр не найдены")
    })
    @DeleteMapping("/{id}/genres/{genreName}")
    public ResponseEntity<BookDetailDTO> deleteGenre(
            @Parameter(description = "Идентификатор книги") @PathVariable @Min(1) Long id,
            @Parameter(description = "Название жанра для удаления") @PathVariable @NotBlank String genreName) {
        return ResponseEntity.ok(bookService.removeGenreFromBook(id, genreName));
    }

    private boolean isAllEmpty(Object... params) {
        return Arrays.stream(params).allMatch(Objects::isNull);
    }
}
