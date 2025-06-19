package LifeValuable.Library.controller;

import LifeValuable.Library.dto.reader.*;
import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.exception.GlobalExceptionHandler;
import LifeValuable.Library.service.ReaderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ReaderControllerTest {
    private MockMvc mockMvc;

    @Mock
    private ReaderService readerService;

    @InjectMocks
    private ReaderController controller;

    private CreateReaderDTO createReaderDTO;
    private ReaderDetailDTO readerDetailDTO;
    private ReaderDTO readerDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        objectMapper = new ObjectMapper();

        createReaderDTO = new CreateReaderDTO(
                "Иван",
                "Петров",
                "ivan.petrov@email.com",
                "+71234567890",
                "password"
        );

        readerDetailDTO = new ReaderDetailDTO(
                1L,
                "Иван",
                "Петров",
                "ivan.petrov@email.com",
                "+71234567890",
                LocalDate.now().minusMonths(6),
                2,
                0,
                5,
                "READER"
        );

        readerDTO = new ReaderDTO(
                1L,
                "Иван Петров",
                "ivan.petrov@email.com",
                "+71234567890",
                LocalDate.now().minusMonths(6),
                2
        );
    }

    private String asJsonString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void whenGetAllReaders_thenReturnPageOfReaders() throws Exception {
        Page<ReaderDTO> readersPage = new PageImpl<>(List.of(readerDTO), PageRequest.of(0, 20), 1);
        when(readerService.findAll(any(Pageable.class))).thenReturn(readersPage);

        mockMvc.perform(get("/api/readers")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "fullName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName").value("Иван Петров"))
                .andExpect(jsonPath("$.content[0].email").value("ivan.petrov@email.com"))
                .andExpect(jsonPath("$.content[0].activeLendingsCount").value(2))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(readerService).findAll(any(Pageable.class));
    }

    @Test
    void whenGetReaderById_thenReturnReaderDetails() throws Exception {
        when(readerService.findById(1L)).thenReturn(readerDetailDTO);

        mockMvc.perform(get("/api/readers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.email").value("ivan.petrov@email.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+71234567890"))
                .andExpect(jsonPath("$.activeLendingsCount").value(2))
                .andExpect(jsonPath("$.overdueLendingsCount").value(0))
                .andExpect(jsonPath("$.totalBorrowed").value(5));

        verify(readerService).findById(1L);
    }

    @Test
    void whenGetReaderById_thenReaderNotFound() throws Exception {
        when(readerService.findById(99L)).thenThrow(new ReaderNotFoundException(99L));

        mockMvc.perform(get("/api/readers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден читатель с id: 99"));

        verify(readerService).findById(99L);
    }

    @Test
    void whenCreateReader_thenReturnReaderDetails() throws Exception {
        when(readerService.create(createReaderDTO)).thenReturn(readerDetailDTO);

        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createReaderDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/readers/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.email").value("ivan.petrov@email.com"));

        verify(readerService).create(createReaderDTO);
    }

    @Test
    void whenCreateReaderWithInvalidData_thenReturnValidationErrors() throws Exception {
        CreateReaderDTO invalidReader = new CreateReaderDTO("", "", "invalid-email", "invalid-phone", "password");

        mockMvc.perform(post("/api/readers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidReader)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("firstName", "lastName", "email", "phoneNumber")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(readerService, never()).create(createReaderDTO);
    }

    @Test
    void whenUpdateReader_thenReturnUpdatedReaderDetails() throws Exception {
        when(readerService.update(createReaderDTO, 1L)).thenReturn(readerDetailDTO);

        mockMvc.perform(put("/api/readers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createReaderDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"));

        verify(readerService).update(createReaderDTO, 1L);
    }

    @Test
    void whenUpdateNonExistentReader_thenReturnNotFound() throws Exception {
        when(readerService.update(createReaderDTO, 99L))
                .thenThrow(new ReaderNotFoundException(99L));

        mockMvc.perform(put("/api/readers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(createReaderDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден читатель с id: 99"));

        verify(readerService).update(createReaderDTO, 99L);
    }

    @Test
    void whenUpdateReaderWithInvalidData_thenReturnValidationErrors() throws Exception {
       CreateReaderDTO invalidReader = new CreateReaderDTO("", "", "invalid-email", "invalid-phone", "password");

        mockMvc.perform(put("/api/readers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidReader)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.violations[*].field",
                        hasItems("firstName", "lastName", "email", "phoneNumber")))
                .andExpect(jsonPath("$.message").value("Ошибка валидации входных данных"));

        verify(readerService, never()).update(any(CreateReaderDTO.class), anyLong());
    }

    @Test
    void whenDeleteReader_thenReturnNoContent() throws Exception {
        doNothing().when(readerService).deleteById(1L);

        mockMvc.perform(delete("/api/readers/1"))
                .andExpect(status().isNoContent());

        verify(readerService).deleteById(1L);
    }

    @Test
    void whenDeleteNonExistentReader_thenReturnNotFound() throws Exception {
        doThrow(new ReaderNotFoundException(99L)).when(readerService).deleteById(99L);

        mockMvc.perform(delete("/api/readers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден читатель с id: 99"));

        verify(readerService).deleteById(99L);
    }

    @Test
    void whenFindReaderByPhoneNumber_thenReturnReaderDetails() throws Exception {
        when(readerService.findByPhoneNumber("+71234567890")).thenReturn(readerDetailDTO);

        mockMvc.perform(get("/api/readers/by-phone")
                        .param("phone", "+71234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.phoneNumber").value("+71234567890"));

        verify(readerService).findByPhoneNumber("+71234567890");
    }

    @Test
    void whenFindReaderByNonExistentPhoneNumber_thenReturnNotFound() throws Exception {
        when(readerService.findByPhoneNumber("+79999999999"))
                .thenThrow(new ReaderNotFoundException("phoneNumber", "+79999999999"));

        mockMvc.perform(get("/api/readers/by-phone")
                        .param("phone", "+79999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден читатель с телефоном: +79999999999"));

        verify(readerService).findByPhoneNumber("+79999999999");
    }

    @Test
    void whenFindReaderByEmail_thenReturnReaderDetails() throws Exception {
        when(readerService.findByEmail("ivan.petrov@email.com")).thenReturn(readerDetailDTO);

        mockMvc.perform(get("/api/readers/by-email")
                        .param("email", "ivan.petrov@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Иван"))
                .andExpect(jsonPath("$.lastName").value("Петров"))
                .andExpect(jsonPath("$.email").value("ivan.petrov@email.com"));

        verify(readerService).findByEmail("ivan.petrov@email.com");
    }

    @Test
    void whenFindReaderByNonExistentEmail_thenReturnNotFound() throws Exception {
        when(readerService.findByEmail("nonexistent@email.com"))
                .thenThrow(new ReaderNotFoundException("email", "nonexistent@email.com"));

        mockMvc.perform(get("/api/readers/by-email")
                        .param("email", "nonexistent@email.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Не найден читатель с почтой: nonexistent@email.com"));

        verify(readerService).findByEmail("nonexistent@email.com");
    }

    @Test
    void whenFindReaderByInvalidEmail_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/readers/by-email")
                        .param("email", ""))
                .andExpect(status().isBadRequest());

        verify(readerService, never()).findByEmail(anyString());
    }

    @Test
    void whenFindReaderByInvalidPhoneNumber_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/readers/by-phone")
                        .param("phone", ""))
                .andExpect(status().isBadRequest());

        verify(readerService, never()).findByPhoneNumber(anyString());
    }
}
