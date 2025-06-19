package LifeValuable.Library.controller;

import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.service.ReaderService;
import LifeValuable.Library.dto.reader.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Читатели", description = "Управление читателями библиотеки")
@RestController
@RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderService readerService;

    @Autowired
    ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @Operation(summary = "Получить всех читателей", description = "Возвращает пагинированный список всех читателей, отсортированный по имени")
    @ApiResponse(responseCode = "200", description = "Список читателей успешно получен")
    @GetMapping
    public ResponseEntity<Page<ReaderDTO>> getAllReaders(
            @Parameter(description = "Параметры пагинации и сортировки")
            @PageableDefault(sort = "firstName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(readerService.findAll(pageable));
    }

    @Operation(summary = "Получить читателя по ID", description = "Возвращает подробную информацию о читателе по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Читатель найден"),
            @ApiResponse(responseCode = "404", description = "Читатель не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReaderDetailDTO> findById(
            @Parameter(description = "Уникальный идентификатор читателя", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(readerService.findById(id));
    }

    @Operation(summary = "Зарегистрировать нового читателя", description = "Создает новую учетную запись читателя в библиотеке")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Читатель успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные читателя"),
            @ApiResponse(responseCode = "409", description = "Читатель с таким email или телефоном уже существует")
    })
    @PostMapping
    public ResponseEntity<ReaderDetailDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные нового читателя", required = true)
            @Valid @RequestBody CreateReaderDTO createReaderDTO) {
        ReaderDetailDTO createdReader = readerService.create(createReaderDTO);
        URI location = URI.create("/api/readers/" + createdReader.id());
        return ResponseEntity.created(location).body(createdReader);
    }

    @Operation(summary = "Обновить данные читателя", description = "Обновляет информацию о читателе по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные читателя успешно обновлены"),
            @ApiResponse(responseCode = "404", description = "Читатель не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных (email или телефон уже используются)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReaderDetailDTO> update(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные читателя", required = true)
            @Valid @RequestBody CreateReaderDTO createReaderDTO,
            @Parameter(description = "Идентификатор читателя") @PathVariable Long id) {
        return ResponseEntity.ok(readerService.update(createReaderDTO, id));
    }


    @Operation(summary = "Удалить читателя", description = "Удаляет читателя из библиотеки по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Читатель успешно удален"),
            @ApiResponse(responseCode = "404", description = "Читатель не найден"),
            @ApiResponse(responseCode = "409", description = "Невозможно удалить читателя с активными выдачами")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Идентификатор читателя для удаления")
            @PathVariable Long id) {
        readerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Поиск читателя по номеру телефона", description = "Находит читателя по его номеру телефона в международном формате")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Читатель найден"),
            @ApiResponse(responseCode = "404", description = "Читатель с таким номером не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат номера телефона")
    })
    @GetMapping("/by-phone")
    public ResponseEntity<ReaderDetailDTO> findByPhoneNumber(
            @RequestParam("phone")
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$", message = "Invalid phone number format")
            @Parameter(
                    description = "Номер телефона в международном формате (начинается с +, содержит 8-15 цифр)",
                    example = "%2B79991234567"
            )
            String number) {
        return ResponseEntity.ok(readerService.findByPhoneNumber(number));
    }

    @Operation(summary = "Поиск читателя по email", description = "Находит читателя по его адресу электронной почты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Читатель найден"),
            @ApiResponse(responseCode = "404", description = "Читатель с таким email не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректный формат email")
    })
    @GetMapping("/by-email")
    public ResponseEntity<ReaderDetailDTO> findByEmail(
            @RequestParam("email")
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            @Parameter(
                    description = "Адрес электронной почты читателя",
                    example = "reader@example.com"
            )
            String email) {
        return ResponseEntity.ok(readerService.findByEmail(email));
    }


    @Operation(summary = "Получение текущего читателя", description = "Возвращает подробную информацию об аутентифицированном читателе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Читатель найден"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация")
    })
    @GetMapping("/me")
    public ResponseEntity<ReaderDetailDTO> getMyProfile(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(readerService.findByEmail(email));
    }

    @Operation(summary = "Обновить данные читателя", description = "Читатель обновляет информацию о себе")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Данные читателя успешно обновлены"),
            @ApiResponse(responseCode = "401", description = "Требуется аутентификация"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Конфликт данных (email или телефон уже используются)")
    })
    @PutMapping("/me")
    public ResponseEntity<ReaderDetailDTO> updateMyProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные читателя", required = true)
            @Valid @RequestBody CreateReaderDTO dto,
            Authentication auth) {

        String email = auth.getName();
        return ResponseEntity.ok(readerService.updateCurrentUser(dto,email));
    }
}
