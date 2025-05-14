package LifeValuable.Library.repository;

import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface LendingRepository extends JpaRepository<Lending, Long> {
    Page<Lending> findByStatus(LendingStatus status, Pageable pageable);
    Page<Lending> findByStatusAndReturnDateIsNull(LendingStatus status, Pageable pageable);
    Page<Lending> findByDueDateBeforeAndReturnDateIsNull(LocalDate date, Pageable pageable);
    Page<Lending> findByLendingDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    Page<Lending> findByReaderId(Long readerId, Pageable pageable);
    Page<Lending> findByReaderIdAndStatus(Long readerId, LendingStatus status, Pageable pageable);
    Page<Lending> findByBookId(Long bookId, Pageable pageable);
    @Query("SELECT new LifeValuable.Library.dto.book.BookPopularityDTO(b.id, b.title, b.author, CAST(COUNT(l) AS Integer)) " +
            "FROM Book b JOIN b.lendings l GROUP BY b.id, b.title, b.author ORDER BY COUNT(l) DESC")
    Page<BookPopularityDTO> findTopBorrowedBooks(Pageable pageable);
}
