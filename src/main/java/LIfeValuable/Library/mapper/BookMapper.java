package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.book.BookDTO;
import LifeValuable.Library.dto.book.BookDetailDTO;
import LifeValuable.Library.dto.book.BookPopularityDTO;
import LifeValuable.Library.dto.book.CreateBookDTO;
import LifeValuable.Library.model.Book;
import LifeValuable.Library.model.Genre;
import LifeValuable.Library.model.LendingStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "genreNames", source = "book", qualifiedByName = "getGenreNames")
    BookDTO toDto(Book book);

    @Mapping(target = "genreNames", source = "book", qualifiedByName = "getGenreNames")
    @Mapping(target = "availableStock", source = "book", qualifiedByName = "getAvailableStock")
    BookDetailDTO toDetailDto(Book book);

    @Mapping(target = "lendingCount", source = "book", qualifiedByName = "getLendingsCount")
    BookPopularityDTO toPopularityDTO(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "genres", ignore = true)
    @Mapping(target = "lendings", ignore = true)
    Book toEntity(CreateBookDTO createBookDTO);

    @Named("getGenreNames")
    default List<String> getGenreNames(Book book) {
        if (book == null || book.getGenres() == null)
            return Collections.emptyList();
        return book.getGenres().stream()
                .map(Genre::getName)
                .toList();
    }

    @Named("getAvailableStock")
    default long getAvailableStock(Book book) {
        if (book == null)
            return 0;
        long activeLendingsCount = 0;
        if (book.getLendings() != null) {
            activeLendingsCount = book.getLendings().stream()
                .filter(lending ->
                        lending.getStatus() == LendingStatus.ACTIVE ||
                        lending.getStatus() == LendingStatus.OVERDUE)
                .count();
        }
        return book.getStock() - activeLendingsCount;
    }

    @Named("getLendingsCount")
    default long getLendingsCount(Book book) {
        if (book == null)
            return 0;
        long lendingsCount = 0;
        if (book.getLendings() != null)
            lendingsCount = book.getLendings().size();
        return lendingsCount;
    }

    List<BookDTO> toDtoList(List<Book> books);
}
