package LifeValuable.Library.dto.book;

import java.util.List;

public record BookDTO(
    Long id,
    String title,
    String author,
    Integer publicationYear,
    Integer stock,
    List<String> genreNames) {
}
