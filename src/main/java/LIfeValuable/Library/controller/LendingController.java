package LifeValuable.Library.controller;

import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.service.LendingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@Validated
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


    @GetMapping
    public ResponseEntity<Page<LendingDTO>> getAllLendings(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(lendingService.findAllLendings(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LendingDetailDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LendingDetailDTO> create(@RequestBody @Valid CreateLendingDTO createLendingDTO) {
        LendingDetailDTO lendingDetailDTO = lendingService.create(createLendingDTO);
        URI location = URI.create("/api/lendings/" + lendingDetailDTO.id());
        return ResponseEntity.created(location).body(lendingDetailDTO);
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<LendingDetailDTO> returnBook(@PathVariable Long id) {
        return ResponseEntity.ok(lendingService.returnBook(id));
    }

    @PatchMapping("/{id}/extend")
    public ResponseEntity<LendingDetailDTO> extendLending(@PathVariable Long id, @RequestBody @Valid ExtendLendingRequest request) {
        return ResponseEntity.ok(lendingService.extendLending(id, request.newDueDate()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LendingDetailDTO> updateStatus(@PathVariable Long id, @RequestBody @Valid LendingStatusRequest request) {
        return ResponseEntity.ok(lendingService.updateLendingStatus(id, request.status()));
    }

    @GetMapping("/by-status")
    public ResponseEntity<Page<LendingDTO>> findByStatus(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam LendingStatus status) {
        return ResponseEntity.ok(lendingService.findByStatus(status, pageable));
    }

    @GetMapping("/overdue")
    public ResponseEntity<Page<LendingDTO>> findOverdueLendings(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam LocalDate date
        ) {
        return ResponseEntity.ok(lendingService.findByDueDateBeforeAndReturnDateIsNull(date, pageable));
    }

    @GetMapping("/by-period")
    public ResponseEntity<Page<LendingDTO>> findByLendingDateBetween(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
        ) {
        return ResponseEntity.ok(lendingService.findByLendingDateBetween(startDate, endDate, pageable));
    }

    @GetMapping("/by-reader/{id}")
    public ResponseEntity<Page<LendingDTO>> findByReaderId(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long id
        ) {
        return ResponseEntity.ok(lendingService.findByReaderId(id, pageable));
    }

    @GetMapping("/by-reader/{id}/overdue")
    public ResponseEntity<Page<LendingDTO>> findOverdueLendingsByReaderId(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(lendingService.getOverdueLendingsForReader(id, pageable));
    }

    @GetMapping("/by-book/{id}")
    public ResponseEntity<Page<LendingDTO>> findByBookId(
            @PageableDefault(sort = "lendingDate", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(lendingService.findByBookId(id, pageable));
    }

    @GetMapping("/popular-books")
    public ResponseEntity<Page<BookPopularityDTO>> findPopularBooks(
            @PageableDefault(sort = "lendingCount", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(lendingService.findTopBorrowedBooks(pageable));
    }
}
