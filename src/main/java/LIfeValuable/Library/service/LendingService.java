package LifeValuable.Library.service;

import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.model.LendingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface LendingService {
    LendingDetailDTO create(CreateLendingDTO createLendingDTO);
    LendingDetailDTO returnBook(Long lendingId);
    LendingDetailDTO findById(Long lendingId);
    Page<LendingDTO> findByStatus(LendingStatus status);

    Page<LendingDTO> findByDueDateBeforeAndReturnDateIsNull(LocalDate date, Pageable pageable);
    Page<LendingDTO> findByLendingDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    Page<LendingDTO> findByReaderId(Long readerId, Pageable pageable);
    Page<LendingDTO> findByReaderIdAndStatus(Long readerId, LendingStatus status, Pageable pageable);
    Page<LendingDTO> findByBookId(Long bookId, Pageable pageable);
    Page<BookPopularityDTO> findTopBorrowedBooks(Pageable pageable);
    
    LendingDetailDTO extendLending(Long lendingId, LocalDate newDueDate); 
    void updateStatuses(); 

    Page<LendingDTO> findAllLendings(Pageable pageable); 
    
    void cancelLending(Long lendingId); 
    LendingDetailDTO updateLendingStatus(Long lendingId, LendingStatus newStatus); 

    boolean isBookAvailableForLending(Long bookId); 
    List<LendingDTO> getOverdueLendingsForReader(Long readerId); 
}
