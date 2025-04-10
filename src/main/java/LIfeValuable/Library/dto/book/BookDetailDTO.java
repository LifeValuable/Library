package LifeValuable.Library.dto.book;

import java.util.List;

public record BookDetailDTO(
    Long id,
    String title,
    String author,
    String isbn,
    Integer publicationYear,
    Integer stock,
    Integer availableStock,
    List<String> genreNames) {
}
