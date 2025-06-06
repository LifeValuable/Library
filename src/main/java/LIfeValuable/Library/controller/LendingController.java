package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.service.LendingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@Tag(name = "Выдачи книг", description = "Управление выдачами и возвратами книг")
@RestController
@RequestMapping("/api/lendings")
public class LendingController {

    private final LendingService lendingService;

    @Autowired
    public LendingController(LendingService lendingService) {
        this.lendingService = lendingService;
    }

    public record ExtendLendingRequest(@FutureOrPresent LocalDate newDueDate) {}
    public record LendingStatusRequest(@NotNull LendingStatus status) {}

    @Operation(summary = "Получить все выдачи", description = "Возвращает пагинированный список всех выдач книг, отсортированный по дате выдачи")
    @ApiResponse(responseCode = "200", description = "Список выдач успешно получен")
    @GetMapping
    public ResponseEntity<Page<LendingDTO>> getAllLendings(
            @Parameter(description = "Параметры пагинации и сортировки")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(lendingService.findAllLendings(pageable));
    }

    @Operation(summary = "Получить выдачу по ID", description = "Возвращает подробную информацию о выдаче по её идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Выдача найдена"),
            @ApiResponse(responseCode = "404", description = "Выдача не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<LendingDetailDTO> findById(
            @Parameter(description = "Уникальный идентификатор выдачи", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(lendingService.findById(id));
    }

    @Operation(summary = "Создать новую выдачу", description = "Оформляет выдачу книги читателю")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Выдача успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные выдачи"),
            @ApiResponse(responseCode = "404", description = "Книга или читатель не найдены")
    })
    @PostMapping
    public ResponseEntity<LendingDetailDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные новой выдачи", required = true)
            @RequestBody @Valid CreateLendingDTO createLendingDTO) {
        LendingDetailDTO lendingDetailDTO = lendingService.create(createLendingDTO);
        URI location = URI.create("/api/lendings/" + lendingDetailDTO.id());
        return ResponseEntity.created(location).body(lendingDetailDTO);
    }

    @Operation(summary = "Вернуть книгу", description = "Оформляет возврат книги, устанавливает дату возврата и обновляет статус")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Книга успешно возвращена"),
            @ApiResponse(responseCode = "404", description = "Выдача не найдена"),
            @ApiResponse(responseCode = "400", description = "Книга уже возвращена или операция невозможна")
    })
    @PostMapping("/{id}/return")
    public ResponseEntity<LendingDetailDTO> returnBook(
            @Parameter(description = "Идентификатор выдачи для возврата")
            @PathVariable Long id) {
        return ResponseEntity.ok(lendingService.returnBook(id));
    }

    @Operation(summary = "Продлить выдачу", description = "Изменяет дату окончания выдачи на новую")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Выдача успешно продлена"),
            @ApiResponse(responseCode = "404", description = "Выдача не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректная дата или операция невозможна")
    })
    @PatchMapping("/{id}/extend")
    public ResponseEntity<LendingDetailDTO> extendLending(
            @Parameter(description = "Идентификатор выдачи") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новая дата окончания выдачи", required = true)
            @RequestBody @Valid ExtendLendingRequest request) {
        return ResponseEntity.ok(lendingService.extendLending(id, request.newDueDate()));
    }

    @Operation(summary = "Обновить статус выдачи", description = "Изменяет статус выдачи (активная, просроченная, возвращена)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Статус успешно обновлен"),
            @ApiResponse(responseCode = "404", description = "Выдача не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректный статус")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<LendingDetailDTO> updateStatus(
            @Parameter(description = "Идентификатор выдачи") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новый статус выдачи", required = true)
            @RequestBody @Valid LendingStatusRequest request) {
        return ResponseEntity.ok(lendingService.updateLendingStatus(id, request.status()));
    }

    @Operation(summary = "Поиск выдач по статусу", description = "Возвращает все выдачи с указанным статусом")
    @ApiResponse(responseCode = "200", description = "Список выдач найден")
    @GetMapping("/by-status")
    public ResponseEntity<Page<LendingDTO>> findByStatus(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Статус выдачи", example = "ACTIVE")
            @RequestParam LendingStatus status) {
        return ResponseEntity.ok(lendingService.findByStatus(status, pageable));
    }

    @Operation(summary = "Найти просроченные выдачи", description = "Возвращает все выдачи, которые просрочены на указанную дату")
    @ApiResponse(responseCode = "200", description = "Список просроченных выдач")
    @GetMapping("/overdue")
    public ResponseEntity<Page<LendingDTO>> findOverdueLendings(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Дата для проверки просрочки", example = "2024-01-15")
            @RequestParam @NotNull LocalDate date) {
        return ResponseEntity.ok(lendingService.findByDueDateBeforeAndReturnDateIsNull(date, pageable));
    }

    @Operation(summary = "Поиск выдач за период", description = "Возвращает выдачи, оформленные в указанном периоде дат")
    @ApiResponse(responseCode = "200", description = "Список выдач за период")
    @GetMapping("/by-period")
    public ResponseEntity<Page<LendingDTO>> findByLendingDateBetween(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Начальная дата периода", example = "2024-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "Конечная дата периода", example = "2024-12-31")
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(lendingService.findByLendingDateBetween(startDate, endDate, pageable));
    }

    @Operation(summary = "Поиск выдач по читателю", description = "Возвращает все выдачи указанного читателя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список выдач читателя"),
            @ApiResponse(responseCode = "404", description = "Читатель не найден")
    })
    @GetMapping("/by-reader/{id}")
    public ResponseEntity<Page<LendingDTO>> findByReaderId(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Идентификатор читателя", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(lendingService.findByReaderId(id, pageable));
    }

    @Operation(summary = "Просроченные выдачи читателя", description = "Возвращает все просроченные выдачи указанного читателя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список просроченных выдач читателя"),
            @ApiResponse(responseCode = "404", description = "Читатель не найден")
    })
    @GetMapping("/by-reader/{id}/overdue")
    public ResponseEntity<Page<LendingDTO>> findOverdueLendingsByReaderId(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Идентификатор читателя", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(lendingService.getOverdueLendingsForReader(id, pageable));
    }

    @Operation(summary = "Поиск выдач по книге", description = "Возвращает все выдачи указанной книги")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список выдач книги"),
            @ApiResponse(responseCode = "404", description = "Книга не найдена")
    })
    @GetMapping("/by-book/{id}")
    public ResponseEntity<Page<LendingDTO>> findByBookId(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @Parameter(description = "Идентификатор книги", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(lendingService.findByBookId(id, pageable));
    }

    @Operation(summary = "Статистика популярных книг", description = "Возвращает книги, отсортированные по количеству выдач (от самых популярных)")
    @ApiResponse(responseCode = "200", description = "Статистика популярности книг")
    @GetMapping("/popular-books")
    public ResponseEntity<Page<BookPopularityDTO>> findPopularBooks(
            @Parameter(description = "Параметры пагинации")
            @PageableDefault(sort = "lendingCount", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(lendingService.findTopBorrowedBooks(pageable));
    }
}
