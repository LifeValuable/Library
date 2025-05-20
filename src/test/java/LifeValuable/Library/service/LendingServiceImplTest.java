package LifeValuable.Library.service;

import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.exception.LendingNotFoundException;
import LifeValuable.Library.mapper.LendingMapper;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.model.Reader;
import LifeValuable.Library.repository.BookLendingProjection;
import LifeValuable.Library.repository.LendingRepository;
import LifeValuable.Library.service.impl.LendingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LendingServiceImplTest {

    @Mock
    private LendingRepository lendingRepository;

    @Mock
    private BookService bookService;

    @Mock
    private ReaderService readerService;

    private final LendingMapper lendingMapper = Mappers.getMapper(LendingMapper.class);

    private LendingServiceImpl lendingService;

    private Lending lending;
    private LendingDTO lendingDTO;
    private LendingDetailDTO lendingDetailDTO;
    private CreateLendingDTO createLendingDTO;
    private BookDetailDTO bookDetailDTO;
    private ReaderDetailDTO readerDetailDTO;
    private BookLendingProjection bookLendingProjection;

    @BeforeEach
    void setUp() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Гарри Поттер и философский камень");
        book.setAuthor("Джоан Роулинг");
        book.setStock(10);

        Reader reader = new Reader();
        reader.setId(1L);
        reader.setFirstName("Иван");
        reader.setLastName("Иванов");

        lending = new Lending();
        lending.setId(1L);
        lending.setBook(book);
        lending.setReader(reader);
        lending.setLendingDate(LocalDate.now().minusDays(7));
        lending.setDueDate(LocalDate.now().plusDays(7));
        lending.setStatus(LendingStatus.ACTIVE);
        lending.setReturnDate(null);
        lendingDetailDTO = lendingMapper.toDetailDto(lending);
        lendingDTO = lendingMapper.toDto(lending);

        createLendingDTO = new CreateLendingDTO(
                book.getId(),
                reader.getId(),
                LocalDate.now(),
                LocalDate.now().plusDays(14)
        );

        bookDetailDTO = new BookDetailDTO(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                "9785389077843",
                1997,
                book.getStock(),
                5,
                List.of("Фэнтези")
        );

        readerDetailDTO = new ReaderDetailDTO(
                reader.getId(),
                reader.getFirstName(),
                reader.getLastName(),
                "example@email.com",
                "123456789",
                LocalDate.now().minusMonths(6),
                0,
                0,
                0
        );

        lendingService = new LendingServiceImpl(lendingRepository, bookService, readerService, lendingMapper);
    }

    @Test
    void whenCreate_thenSaveAndReturnLendingDetailDTO() {
        when(readerService.findById(createLendingDTO.readerId())).thenReturn(readerDetailDTO);
        when(bookService.findById(createLendingDTO.bookId())).thenReturn(bookDetailDTO);
        when(lendingRepository.save(any(Lending.class))).thenReturn(lending);

        LendingDetailDTO result = lendingService.create(createLendingDTO);

        assertThat(result).isEqualTo(lendingDetailDTO);
        verify(readerService).findById(createLendingDTO.readerId());
        verify(bookService).findById(createLendingDTO.bookId());
        verify(lendingRepository).save(any(Lending.class));
    }

    @Test
    void whenCreate_withUnavailableBook_thenThrowException() {
        BookDetailDTO unavailableBook = new BookDetailDTO(
                bookDetailDTO.id(), bookDetailDTO.title(), bookDetailDTO.author(),
                bookDetailDTO.isbn(), bookDetailDTO.publicationYear(), bookDetailDTO.stock(),
                0, bookDetailDTO.genreNames()
        );

        when(readerService.findById(createLendingDTO.readerId())).thenReturn(readerDetailDTO);
        when(bookService.findById(createLendingDTO.bookId())).thenReturn(unavailableBook);

        assertThatThrownBy(() -> lendingService.create(createLendingDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("isn't available for lending");

        verify(readerService).findById(createLendingDTO.readerId());
        verify(bookService).findById(createLendingDTO.bookId());
        verify(lendingRepository, never()).save(any(Lending.class));
    }

    @Test
    void whenReturnBook_withExistingLending_thenUpdateStatusAndReturnDate() {
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        LendingDetailDTO result = lendingService.returnBook(lending.getId());

        assertThat(result.status()).isEqualTo(LendingStatus.RETURNED);
        assertThat(result.returnDate()).isEqualTo(LocalDate.now());
        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenReturnBook_withNonExistingLending_thenThrowLendingNotFoundException() {
        when(lendingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lendingService.returnBook(99L))
                .isInstanceOf(LendingNotFoundException.class)
                .hasMessageContaining("Lending not found with id: 99");

        verify(lendingRepository).findById(99L);
    }

    @Test
    void whenReturnBook_withAlreadyReturnedLending_thenThrowException() {
        lending.setStatus(LendingStatus.RETURNED);
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        assertThatThrownBy(() -> lendingService.returnBook(lending.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("is already returned");

        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenFindById_withExistingLending_thenReturnLendingDetailDTO() {
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        LendingDetailDTO result = lendingService.findById(lending.getId());

        assertThat(result).isEqualTo(lendingDetailDTO);
        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenFindById_withNonExistingLending_thenThrowLendingNotFoundException() {
        when(lendingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lendingService.findById(99L))
                .isInstanceOf(LendingNotFoundException.class);

        verify(lendingRepository).findById(99L);
    }

    @Test
    void whenFindByStatus_thenReturnPageOfLendingDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByStatus(LendingStatus.ACTIVE, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByStatus(LendingStatus.ACTIVE, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).status()).isEqualTo(LendingStatus.ACTIVE);
        verify(lendingRepository).findByStatus(LendingStatus.ACTIVE, pageable);
    }

    @Test
    void whenFindByDueDateBeforeAndReturnDateIsNull_thenReturnPageOfLendingDTO() {
        LocalDate date = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByDueDateBeforeAndReturnDateIsNull(date, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByDueDateBeforeAndReturnDateIsNull(date, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(lendingRepository).findByDueDateBeforeAndReturnDateIsNull(date, pageable);
    }

    @Test
    void whenFindByLendingDateBetween_thenReturnPageOfLendingDTO() {
        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate end = LocalDate.now();
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByLendingDateBetween(start, end, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByLendingDateBetween(start, end, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(lendingRepository).findByLendingDateBetween(start, end, pageable);
    }

    @Test
    void whenFindByReaderId_thenReturnPageOfLendingDTO() {
        Long readerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByReaderId(readerId, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByReaderId(readerId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).readerFullName()).contains(lending.getReader().getFirstName());
        assertThat(result.getContent().get(0).readerFullName()).contains(lending.getReader().getLastName());
        assertThat(result.getContent().get(0).status()).isEqualTo(LendingStatus.ACTIVE);
        assertThat(result.getContent().get(0).isOverdue()).isNotNull();
        assertThat(result.getContent().get(0).daysLeft()).isNotNull();
        verify(lendingRepository).findByReaderId(readerId, pageable);
    }

    @Test
    void whenFindByReaderIdAndStatus_thenReturnPageOfLendingDTO() {
        Long readerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByReaderIdAndStatus(readerId, LendingStatus.ACTIVE, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByReaderIdAndStatus(readerId, LendingStatus.ACTIVE, pageable);
        LendingDTO expected = lendingMapper.toDto(lending);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0)).isEqualTo(expected);

        verify(lendingRepository).findByReaderIdAndStatus(readerId, LendingStatus.ACTIVE, pageable);
    }


    @Test
    void whenFindByBookId_thenReturnPageOfLendingDTO() {
        Long bookId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByBookId(bookId, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findByBookId(bookId, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(bookId);
        verify(lendingRepository).findByBookId(bookId, pageable);
    }

    @Test
    void whenFindTopBorrowedBooks_thenReturnPageOfBookPopularityDTO() {
        Pageable pageable = PageRequest.of(0, 10);

        BookLendingProjection projection = mock(BookLendingProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getTitle()).thenReturn("Гарри Поттер и философский камень");
        when(projection.getAuthor()).thenReturn("Джоан Роулинг");
        when(projection.getLendingCount()).thenReturn(5);

        Page<BookLendingProjection> projections = new PageImpl<>(List.of(projection), pageable, 1);
        when(lendingRepository.findTopBorrowedBooks(pageable)).thenReturn(projections);

        Page<BookPopularityDTO> result = lendingService.findTopBorrowedBooks(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(1L);
        assertThat(result.getContent().get(0).lendingCount()).isEqualTo(5);
        verify(lendingRepository).findTopBorrowedBooks(pageable);
    }

    @Test
    void whenExtendLending_withExistingLending_thenUpdateDueDate() {
        LocalDate newDueDate = lending.getDueDate().plusDays(7);
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        LendingDetailDTO result = lendingService.extendLending(lending.getId(), newDueDate);

        assertThat(result.dueDate()).isEqualTo(newDueDate);
        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenExtendLending_withEarlierDueDate_thenThrowException() {
        LocalDate earlierDueDate = lending.getDueDate().minusDays(1);
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        assertThatThrownBy(() -> lendingService.extendLending(lending.getId(), earlierDueDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("New due date can't be before previous due date");

        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenUpdateStatuses_thenOverdueStatusesUpdated() {
        Lending overdueLending = new Lending();
        overdueLending.setId(2L);
        overdueLending.setStatus(LendingStatus.ACTIVE);
        overdueLending.setDueDate(LocalDate.now().minusDays(1));

        List<Lending> lendings = new ArrayList<>();
        lendings.add(lending);
        lendings.add(overdueLending);

        when(lendingRepository.findAll()).thenReturn(lendings);

        lendingService.updateStatuses();

        assertThat(overdueLending.getStatus()).isEqualTo(LendingStatus.OVERDUE);
        assertThat(lending.getStatus()).isEqualTo(LendingStatus.ACTIVE); // не должен измениться
        verify(lendingRepository).findAll();
    }

    @Test
    void whenFindAllLendings_thenReturnPageOfLendingDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findAll(pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.findAllLendings(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(lendingRepository).findAll(pageable);
    }

    @Test
    void whenUpdateLendingStatus_withExistingLending_thenUpdateStatus() {
        when(lendingRepository.findById(lending.getId())).thenReturn(Optional.of(lending));

        LendingDetailDTO result = lendingService.updateLendingStatus(lending.getId(), LendingStatus.OVERDUE);

        assertThat(result.status()).isEqualTo(LendingStatus.OVERDUE);
        verify(lendingRepository).findById(lending.getId());
    }

    @Test
    void whenGetOverdueLendingsForReader_thenReturnPageOfLendingDTO() {
        Long readerId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Lending> lendings = List.of(lending);
        Page<Lending> lendingPage = new PageImpl<>(lendings, pageable, lendings.size());

        when(lendingRepository.findByReaderIdAndStatus(readerId, LendingStatus.OVERDUE, pageable)).thenReturn(lendingPage);

        Page<LendingDTO> result = lendingService.getOverdueLendingsForReader(readerId, pageable);

        assertThat(result).isNotNull();
        verify(lendingRepository).findByReaderIdAndStatus(readerId, LendingStatus.OVERDUE, pageable);
    }
}
