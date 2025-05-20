package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.exception.BookNotFoundException;
import LifeValuable.Library.exception.LendingNotFoundException;
import LifeValuable.Library.mapper.LendingMapper;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.repository.LendingRepository;
import LifeValuable.Library.service.BookService;
import LifeValuable.Library.service.LendingService;
import LifeValuable.Library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public class LendingServiceImpl implements LendingService {
    private final LendingMapper lendingMapper;
    private final LendingRepository lendingRepository;
    private final BookService bookService;
    private final ReaderService readerService;

    @Autowired
    public LendingServiceImpl(LendingRepository lendingRepository, BookService bookService, ReaderService readerService, LendingMapper lendingMapper) {
        this.lendingRepository = lendingRepository;
        this.lendingMapper = lendingMapper;
        this.bookService = bookService;
        this.readerService = readerService;
    }

    @Override
    public LendingDetailDTO create(CreateLendingDTO createLendingDTO) {
        readerService.findById(createLendingDTO.readerId());
        BookDetailDTO bookDTO = bookService.findById(createLendingDTO.bookId());
        if (bookDTO.availableStock() == 0)
            throw new RuntimeException(String.format("Book with id %d isn't available for lending", bookDTO.id()));

        Lending lending = lendingMapper.toEntity(createLendingDTO);
        lending.setStatus(LendingStatus.ACTIVE);

        Lending savedLending = lendingRepository.save(lending);
        return lendingMapper.toDetailDto(savedLending);
    }

    @Transactional
    @Override
    public LendingDetailDTO returnBook(Long lendingId) {
        Lending lending = lendingRepository.findById(lendingId).orElseThrow(() -> new LendingNotFoundException(lendingId));

        if (lending.getStatus().equals(LendingStatus.RETURNED))
            throw new RuntimeException(String.format("Lending with id %d is already returned", lendingId));

        lending.setStatus(LendingStatus.RETURNED);
        lending.setReturnDate(LocalDate.now());

        return lendingMapper.toDetailDto(lending);
    }

    @Override
    public LendingDetailDTO findById(Long lendingId) {
        Lending lending = lendingRepository.findById(lendingId).orElseThrow(() -> new LendingNotFoundException(lendingId));
        return lendingMapper.toDetailDto(lending);
    }

    @Override
    public Page<LendingDTO> findByStatus(LendingStatus status, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByStatus(status, pageable);
        return lendings.map(lendingMapper::toDto);
    }


    @Override
    public Page<LendingDTO> findByDueDateBeforeAndReturnDateIsNull(LocalDate date, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByDueDateBeforeAndReturnDateIsNull(date, pageable);
        return lendings.map(lendingMapper::toDto);
    }

    @Override
    public Page<LendingDTO> findByLendingDateBetween(LocalDate start, LocalDate end, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByLendingDateBetween(start, end, pageable);
        return lendings.map(lendingMapper::toDto);
    }

    @Override
    public Page<LendingDTO> findByReaderId(Long readerId, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByReaderId(readerId, pageable);
        return lendings.map(lendingMapper::toDto);
    }

    @Override
    public Page<LendingDTO> findByReaderIdAndStatus(Long readerId, LendingStatus status, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByReaderIdAndStatus(readerId, status, pageable);
        return lendings.map(lendingMapper::toDto);
    }

    @Override
    public Page<LendingDTO> findByBookId(Long bookId, Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findByBookId(bookId, pageable);
        return lendings.map(lendingMapper::toDto);
    }

    @Override
    public Page<BookPopularityDTO> findTopBorrowedBooks(Pageable pageable) {
        return lendingRepository.findTopBorrowedBooks(pageable).map(
                projection -> new BookPopularityDTO(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getAuthor(),
                        projection.getLendingCount())
        );
    }


    @Override
    public LendingDetailDTO extendLending(Long lendingId, LocalDate newDueDate) {
        Lending lending = lendingRepository.findById(lendingId).orElseThrow(() -> new LendingNotFoundException(lendingId));
        if (newDueDate.isBefore(lending.getDueDate()))
            throw new RuntimeException("New due date can't be before previous due date");

        lending.setDueDate(newDueDate);
        return lendingMapper.toDetailDto(lending);
    }

    @Override
    public void updateStatuses() {
        List<Lending> lendings = lendingRepository.findAll();
        for (Lending lending : lendings) {
            if (lending.getStatus().equals(LendingStatus.ACTIVE) && lending.getDueDate().isBefore(LocalDate.now())) {
               lending.setStatus(LendingStatus.OVERDUE);
            }
        }
    }


    @Override
    public Page<LendingDTO> findAllLendings(Pageable pageable) {
        Page<Lending> lendings = lendingRepository.findAll(pageable);
        return lendings.map(lendingMapper::toDto);
    }


    @Override
    public LendingDetailDTO updateLendingStatus(Long lendingId, LendingStatus newStatus) {
        Lending lending = lendingRepository.findById(lendingId).orElseThrow(() -> new LendingNotFoundException(lendingId));
        lending.setStatus(newStatus);
        return lendingMapper.toDetailDto(lending);
    }

    @Override
    public Page<LendingDTO> getOverdueLendingsForReader(Long readerId, Pageable pageable) {
        return findByReaderIdAndStatus(readerId, LendingStatus.OVERDUE, pageable);
    }
}
