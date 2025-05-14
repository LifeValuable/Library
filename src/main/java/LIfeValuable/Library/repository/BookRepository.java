package LifeValuable.Library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;


public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);
    Page<Book> findByTitleContaining(String titleFragment, Pageable pageable);
    Page<Book> findByAuthor(String author, Pageable pageable);
    Optional<Book> findByIsbn(String isbn);
    Page<Book> findByPublicationYear(Integer year, Pageable pageable);
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g IN :genres GROUP BY b HAVING COUNT(DISTINCT g) = :#{#genres.size()}")
    Page<Book> findByAllGenres(@Param("genres") List<Genre> genres, Pageable pageable);


    Page<Book> findByPublicationYearBetween(Integer start, Integer end, Pageable pageable);
    Page<Book> findByStockGreaterThan(Integer min, Pageable pageable);
    Page<Book> findByStockLessThan(Integer min, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.name = :name")
    Page<Book> findByGenreName(@Param("name") String genreName, Pageable pageable);
}
