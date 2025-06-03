package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.exception.BookNotFoundException;
import LifeValuable.Library.dto.exception.ErrorResponse;
import LifeValuable.Library.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

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


    @GetMapping
    public ResponseEntity<Page<BookDTO>> getAllBooks(
            @PageableDefault(sort = "title", direction = Sort.Direction.ASC)
                 Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDetailDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.findById(id));
    }

    @PostMapping
    public ResponseEntity<BookDetailDTO> create(@Valid @RequestBody CreateBookDTO bookDTO) {
        BookDetailDTO created = bookService.create(bookDTO);
        URI location = URI.create("/api/books/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDetailDTO> update(@PathVariable Long id, @Valid @RequestBody CreateBookDTO bookDTO) {
        return ResponseEntity.ok(bookService.update(bookDTO, id));
    }

    @GetMapping(value = "/search", params = {"title", "!titleFragment"})
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        return bookService.findByTitle(title)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/search", params = "isbn")
    public ResponseEntity<?> searchByIsbn(@RequestParam String isbn) {
        return bookService.findByIsbn(isbn)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/search", params = {"titleFragment", "!title"})
    public ResponseEntity<Page<BookDTO>> searchByTitleFragment(
            @RequestParam String titleFragment,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByTitleContaining(titleFragment, pageable));
    }

    @GetMapping(value = "/search", params = "author")
    public ResponseEntity<Page<BookDTO>> searchByAuthor(
            @RequestParam String author,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAuthor(author, pageable));
    }

    @GetMapping(value = "/search", params = "publicationYear")
    public ResponseEntity<Page<BookDTO>> searchByPublicationYear(
            @RequestParam Integer publicationYear,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByPublicationYear(publicationYear, pageable));
    }

    @GetMapping(value = "/search", params = {"startYear", "endYear"})
    public ResponseEntity<Page<BookDTO>> searchByYearRange(
            @RequestParam Integer startYear,
            @RequestParam Integer endYear,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByPublicationYearBetween(startYear, endYear, pageable));
    }

    @GetMapping(value = "/search", params = "genre")
    public ResponseEntity<Page<BookDTO>> searchByGenre(
            @RequestParam String genre,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByGenreName(genre, pageable));
    }

    @GetMapping(value = "/search", params = "genres")
    public ResponseEntity<Page<BookDTO>> searchByGenres(
            @RequestParam List<String> genres,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findByAllGenres(genres, pageable));
    }

    @GetMapping(value = "/search")
    public ResponseEntity<Page<BookDTO>> searchAll(@PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(bookService.findAll(pageable));
    }



    @PatchMapping("/{id}/stock")
    public ResponseEntity<BookDetailDTO> updateStock(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(bookService.updateBookStock(id, request.stock()));
    }

    @PostMapping("/{id}/genres")
    public ResponseEntity<BookDetailDTO> addGenre(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody GenreRequest request) {
        return ResponseEntity.ok(bookService.addGenreToBook(id, request.genreName()));
    }

    @DeleteMapping("/{id}/genres/{genreName}")
    public ResponseEntity<BookDetailDTO> deleteGenre(
            @PathVariable @Min(1) Long id,
            @PathVariable @NotBlank String genreName) {
        return ResponseEntity.ok(bookService.removeGenreFromBook(id, genreName));
    }


    private boolean isAllEmpty(Object... params) {
        return Arrays.stream(params).allMatch(Objects::isNull);
    }

}

