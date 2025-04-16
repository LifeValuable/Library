package LifeValuable.Library.dto.book;

public record BookPopularityDTO(
        Long id,
        String title,
        String author,
        Integer lendingCount) {
}