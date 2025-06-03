package LifeValuable.Library.service;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface BookService {
    BookDetailDTO create(CreateBookDTO createBookDTO);
    BookDetailDTO update(CreateBookDTO createBookDTO, Long id);
    void deleteById(Long id);
    BookDetailDTO findById(Long id);
    Page<BookDTO> findAll(Pageable pageable);

    Optional<BookDetailDTO> findByTitle(String title);
    Page<BookDTO> findByTitleContaining(String titleFragment, Pageable pageable);
    Page<BookDTO> findByAuthor(String author, Pageable pageable);
    Optional<BookDetailDTO> findByIsbn(String isbn);
    Page<BookDTO> findByPublicationYear(Integer year, Pageable pageable);
    Page<BookDTO> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);

    Page<BookDTO> findByGenreName(String genreName, Pageable pageable);
    Page<BookDTO> findByAllGenres(List<String> genreNames, Pageable pageable);
    BookDetailDTO addGenreToBook(Long bookId, String genreName);
    BookDetailDTO removeGenreFromBook(Long bookId, String genreName);

    BookDetailDTO updateBookStock(Long id, Integer newStock);
    Page<BookDTO> findByStockAvailable(Pageable pageable);
    Page<BookDTO> findByStockGreaterThan(Integer minStock, Pageable pageable);
    Page<BookDTO> findByStockLessThan(Integer maxStock, Pageable pageable);

    boolean isAvailableForLending(Long bookId);
    int getAvailableStockCount(Long bookId);
}
