package LifeValuable.Library.dto.book;

import java.util.List;

import org.hibernate.validator.constraints.ISBN;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CreateBookDTO(
    @NotBlank String title,
    @NotBlank String author,
    @ISBN String isbn,
    @Min(0) Integer publicationYear,
    @Min(0) Integer stock,
    @NotEmpty List<String> genreNames) {
}
