package LifeValuable.Library.repository;

import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LendingRepository extends JpaRepository<Lending, Long> {
    List<Lending> findByStatus(LendingStatus status);
    List<Lending> findByStatusAndReturnDateIsNull(LendingStatus status);
    List<Lending> findByDueDateBeforeAndReturnDateIsNull(LocalDate date);
    List<Lending> findByLendingDateBetween(LocalDate start, LocalDate end);
    List<Lending> findByReaderId(@Param("reader_id") Long readerId);
    List<Lending> findByReaderIdAndStatus(Long readerId, LendingStatus status);
    List<Lending> findByBookId(Long bookId);
    @Query("SELECT l.book, COUNT(l) FROM Lending l GROUP BY l.book ORDER BY COUNT(l) DESC")
    List<Object[]> findTopBorrowedBooks(Pageable pageable);
}
