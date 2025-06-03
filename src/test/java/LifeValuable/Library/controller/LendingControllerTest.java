package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.*;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.exception.LendingNotFoundException;
import LifeValuable.Library.exception.GlobalExceptionHandler;
import LifeValuable.Library.service.LendingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class LendingControllerTest {
    private MockMvc mockMvc;

    @Mock
    private LendingService lendingService;

    @InjectMocks
    private LendingController controller;

    private CreateLendingDTO createLendingDTO;
    private LendingDetailDTO lendingDetailDTO;
    private LendingDTO lendingDTO;
    private BookPopularityDTO bookPopularityDTO;
    private ObjectMapper objectMapper;

    private LocalDate lendingDate;
    private LocalDate dueDate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        lendingDate = LocalDate.now().minusDays(5);
        dueDate = LocalDate.now().plusDays(9);

        createLendingDTO = new CreateLendingDTO(
                2L,
                1L,
                lendingDate,
                dueDate
        );

        lendingDetailDTO = new LendingDetailDTO(
                1L,
                2L,
                "Война и мир",
                "978-5-389-07784-3",
                1L,
                "Иван Петров",
                "ivan.petrov@email.com",
                lendingDate,
                dueDate,
                null,
                LendingStatus.ACTIVE
        );

        lendingDTO = new LendingDTO(
                1L,
                "Война и мир",
                "Иван Петров",
                lendingDate,
                dueDate,
                LendingStatus.ACTIVE,
                false,
                9 
        );

        bookPopularityDTO = new BookPopularityDTO(
                2L,
                "Война и мир",
                "Лев Толстой",
                5
        );
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void whenGetAllLendings_thenReturnPageOfLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.findAllLendings(any(Pageable.class))).thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "lendingDate,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].readerFullName").value("Иван Петров"))
                .andExpect(jsonPath("$.content[0].bookTitle").value("Война и мир"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].isOverdue").value(false))
                .andExpect(jsonPath("$.content[0].daysLeft").value(9))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(lendingService).findAllLendings(any(Pageable.class));
    }

    @Test
    void whenGetLendingById_thenReturnLendingDetails() throws Exception {
        when(lendingService.findById(1L)).thenReturn(lendingDetailDTO);

        mockMvc.perform(get("/api/lendings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.bookTitle").value("Война и мир"))
                .andExpect(jsonPath("$.bookIsbn").value("978-5-389-07784-3"))
                .andExpect(jsonPath("$.readerId").value(1))
                .andExpect(jsonPath("$.readerFullName").value("Иван Петров"))
                .andExpect(jsonPath("$.readerEmail").value("ivan.petrov@email.com"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.returnDate").isEmpty());

        verify(lendingService).findById(1L);
    }

    @Test
    void whenGetLendingById_thenLendingNotFound() throws Exception {
        when(lendingService.findById(99L)).thenThrow(new LendingNotFoundException(99L));

        mockMvc.perform(get("/api/lendings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена выдача с id: 99"));

        verify(lendingService).findById(99L);
    }

    @Test
    void whenCreateLending_thenReturnLendingDetails() throws Exception {
        when(lendingService.create(createLendingDTO)).thenReturn(lendingDetailDTO);

        System.out.println("asJsonString(createLendingDTO)" + asJsonString(createLendingDTO));
        System.out.println("lendingDetailDTO" + lendingDetailDTO);
        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createLendingDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/lendings/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.bookId").value(2))
                .andExpect(jsonPath("$.readerId").value(1))
                .andExpect(jsonPath("$.bookTitle").value("Война и мир"))
                .andExpect(jsonPath("$.readerFullName").value("Иван Петров"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(lendingService).create(createLendingDTO);
    }

    @Test
    void whenCreateLendingWithInvalidData_thenReturnValidationErrors() throws Exception {
        CreateLendingDTO invalidLending = new CreateLendingDTO(
                null,
                null,
                LocalDate.now().plusDays(1),
                null
        );

        mockMvc.perform(post("/api/lendings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidLending)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("bookId", "readerId", "lendingDate", "dueDate")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(lendingService, never()).create(any(CreateLendingDTO.class));
    }

    @Test
    void whenReturnBook_thenReturnUpdatedLendingDetails() throws Exception {
        LendingDetailDTO returnedLending = new LendingDetailDTO(
                1L, 2L, "Война и мир", "978-5-389-07784-3",
                1L, "Иван Петров", "ivan.petrov@email.com",
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7),
                LocalDate.now(), LendingStatus.RETURNED
        );

        when(lendingService.returnBook(1L)).thenReturn(returnedLending);

        mockMvc.perform(post("/api/lendings/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnDate").isNotEmpty());

        verify(lendingService).returnBook(1L);
    }

    @Test
    void whenReturnNonExistentLending_thenReturnNotFound() throws Exception {
        when(lendingService.returnBook(99L)).thenThrow(new LendingNotFoundException(99L));

        mockMvc.perform(post("/api/lendings/99/return"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена выдача с id: 99"));

        verify(lendingService).returnBook(99L);
    }

    @Test
    void whenExtendLending_thenReturnUpdatedLendingDetails() throws Exception {
        LocalDate newDueDate = LocalDate.now().plusDays(21);
        LendingDetailDTO extendedLending = new LendingDetailDTO(
                1L, 2L, "Война и мир", "978-5-389-07784-3",
                1L, "Иван Петров", "ivan.petrov@email.com",
                LocalDate.now().minusDays(7), newDueDate,
                null, LendingStatus.ACTIVE
        );

        when(lendingService.extendLending(1L, newDueDate)).thenReturn(extendedLending);

        mockMvc.perform(patch("/api/lendings/1/extend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newDueDate\": \"" + newDueDate + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dueDate").value(newDueDate.toString()));

        verify(lendingService).extendLending(1L, newDueDate);
    }

    @Test
    void whenExtendNonExistentLending_thenReturnNotFound() throws Exception {
        LocalDate newDueDate = LocalDate.now().plusDays(21);

        when(lendingService.extendLending(99L, newDueDate))
                .thenThrow(new LendingNotFoundException(99L));

        mockMvc.perform(patch("/api/lendings/99/extend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newDueDate\": \"" + newDueDate + "\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найдена выдача с id: 99"));

        verify(lendingService).extendLending(99L, newDueDate);
    }

    @Test
    void whenUpdateLendingStatus_thenReturnUpdatedLendingDetails() throws Exception {
        LendingDetailDTO updatedLending = new LendingDetailDTO(
                1L, 2L, "Война и мир", "978-5-389-07784-3",
                1L, "Иван Петров", "ivan.petrov@email.com",
                LocalDate.now().minusDays(7), LocalDate.now().plusDays(7),
                null, LendingStatus.OVERDUE
        );

        System.out.println("UPDATED LENDING" + updatedLending.toString());

        when(lendingService.updateLendingStatus(1L, LendingStatus.OVERDUE)).thenReturn(updatedLending);

        mockMvc.perform(patch("/api/lendings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"OVERDUE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("OVERDUE"));

        verify(lendingService).updateLendingStatus(1L, LendingStatus.OVERDUE);
    }

    @Test
    void whenFindLendingsByStatus_thenReturnMatchingLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.findByStatus(LendingStatus.ACTIVE, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")))).thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/by-status")
                        .param("status", "ACTIVE")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));

        verify(lendingService).findByStatus(LendingStatus.ACTIVE, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindOverdueLendings_thenReturnOverdueLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        LocalDate currentDate = LocalDate.now();

        when(lendingService.findByDueDateBeforeAndReturnDateIsNull(currentDate, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate"))))
                .thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/overdue")
                        .param("date", currentDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(lendingService).findByDueDateBeforeAndReturnDateIsNull(currentDate, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindLendingsByPeriod_thenReturnLendingsInPeriod() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(lendingService.findByLendingDateBetween(startDate, endDate, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate"))))
                .thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/by-period")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(lendingService).findByLendingDateBetween(startDate, endDate, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindLendingsByReaderId_thenReturnReaderLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.findByReaderId(1L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")))).thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/by-reader/{id}", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].readerFullName").value("Иван Петров"));

        verify(lendingService).findByReaderId(1L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindOverdueLendingsByReaderId_thenReturnReaderOverdueLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.getOverdueLendingsForReader(1L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")))).thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/by-reader/{id}/overdue", 1L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        verify(lendingService).getOverdueLendingsForReader(1L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindLendingsByBookId_thenReturnBookLendings() throws Exception {
        Page<LendingDTO> lendingsPage = new PageImpl<>(List.of(lendingDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.findByBookId(2L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")))).thenReturn(lendingsPage);

        mockMvc.perform(get("/api/lendings/by-book/{id}", 2L)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].bookTitle").value("Война и мир"));

        verify(lendingService).findByBookId(2L, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")));
    }

    @Test
    void whenFindTopBorrowedBooks_thenReturnPopularBooks() throws Exception {
        Page<BookPopularityDTO> popularBooksPage = new PageImpl<>(List.of(bookPopularityDTO), PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingDate")), 1);
        when(lendingService.findTopBorrowedBooks(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingCount")))).thenReturn(popularBooksPage);

        mockMvc.perform(get("/api/lendings/popular-books")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Война и мир"))
                .andExpect(jsonPath("$.content[0].author").value("Лев Толстой"))
                .andExpect(jsonPath("$.content[0].lendingCount").value(5));

        verify(lendingService).findTopBorrowedBooks(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "lendingCount")));
    }


    @Test
    void whenCreateLendingWithFutureLendingDate_thenReturnValidationError() throws Exception {
        CreateLendingDTO invalidLending = new CreateLendingDTO(
                2L, 1L,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(14)
        );

        mockMvc.perform(post("/api/lendings")
                         .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidLending)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations[*].field", hasItem("lendingDate")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(lendingService, never()).create(any(CreateLendingDTO.class));
    }

    @Test
    void whenExtendLendingWithInvalidDate_thenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/lendings/1/extend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newDueDate\": \"invalid-date\"}"))
                .andExpect(status().isBadRequest());

        verify(lendingService, never()).extendLending(anyLong(), any(LocalDate.class));
    }

    @Test
    void whenUpdateLendingStatusWithInvalidStatus_thenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/api/lendings/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());

        verify(lendingService, never()).updateLendingStatus(anyLong(), any(LendingStatus.class));
    }
}
