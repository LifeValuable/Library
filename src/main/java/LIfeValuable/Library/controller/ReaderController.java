package LifeValuable.Library.controller;

import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.service.ReaderService;
import LifeValuable.Library.dto.reader.*;
import LifeValuable.Library.dto.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/readers")
public class ReaderController {
    private final ReaderService readerService;

    @Autowired
    ReaderController(ReaderService readerService) {
        this.readerService = readerService;
    }

    @GetMapping
    public ResponseEntity<Page<ReaderDTO>> getAllReaders (
            @PageableDefault(sort = "fullName", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(readerService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReaderDetailDTO> findById (@PathVariable Long id) {
        return ResponseEntity.ok(readerService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ReaderDetailDTO> create (@Valid @RequestBody CreateReaderDTO createReaderDTO) {
        ReaderDetailDTO createdReader = readerService.create(createReaderDTO);
        URI location = URI.create("/api/readers/" + createdReader.id());
        return ResponseEntity.created(location).body(createdReader);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReaderDetailDTO> update (@Valid @RequestBody CreateReaderDTO createReaderDTO, @PathVariable Long id) {
        return ResponseEntity.ok(readerService.update(createReaderDTO, id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById (@PathVariable Long id) {
        readerService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-phone")
    public ResponseEntity<ReaderDetailDTO> findByPhoneNumber (@RequestParam("phone")
                                                             @Pattern(regexp = "^\\+[1-9][0-9]{7,14}$")
                                                             String number) {
        return ResponseEntity.ok(readerService.findByPhoneNumber(number));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ReaderDetailDTO> findByEmail (@RequestParam("email")
                                                             @NotBlank @Email
                                                             String email) {
        return ResponseEntity.ok(readerService.findByEmail(email));
    }
}
