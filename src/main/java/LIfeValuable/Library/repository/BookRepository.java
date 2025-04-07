package LifeValuable.Library.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;


public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitle(String title);
    List<Book> findByTitleContaining(String titleFragment);
    List<Book> findByAuthor(String author);
    Optional<Book> findByIsbn(String isbn);
    List<Book> findByPublicationYear(Integer year);
    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g IN :genres GROUP BY b HAVING COUNT(DISTINCT g) = :genresCount")
    List<Book> findByAllGenres(@Param("genres") List<Genre> genres, @Param("genresCount") Long genresCount);

    List<Book> findByPublicationYearBetween(Integer start, Integer end);
    List<Book> findByStockGreaterThan(Integer min);
    List<Book> findByStockLessThan(Integer min);

    @Query("SELECT b FROM Book b JOIN b.genres g WHERE g.name = :name")
    List<Book> findByGenreName(@Param("name") String genreName);
}
