package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.lending.CreateLendingDTO;
import LifeValuable.Library.dto.lending.LendingDTO;
import LifeValuable.Library.dto.lending.LendingDetailDTO;
import LifeValuable.Library.model.Lending;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.model.Reader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface LendingMapper {

    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "readerFullName", source = "lending", qualifiedByName = "getReaderFullName")
    @Mapping(target = "isOverdue", source = "lending", qualifiedByName = "isOverdue")
    @Mapping(target = "daysLeft", source = "lending", qualifiedByName = "getDaysLeft")
    LendingDTO toDto(Lending lending);

    @Mapping(target = "bookId", source = "book.id")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "bookIsbn", source = "book.isbn")
    @Mapping(target = "readerId", source = "reader.id")
    @Mapping(target = "readerFullName", source = "lending", qualifiedByName = "getReaderFullName")
    @Mapping(target = "readerEmail", source = "reader.email")
    LendingDetailDTO toDetailDto(Lending lending);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "reader", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "returnDate", ignore = true)
    Lending toEntity(CreateLendingDTO createLendingDTO);

    @Named("getReaderFullName")
    default String getReaderFullName(Lending lending) {
        if (lending == null || lending.getReader() == null)
            return "";

        Reader reader = lending.getReader();
        return reader.getFirstName() + " " + reader.getLastName();
    }

    @Named("isOverdue")
    default boolean isOverdue(Lending lending) {
        if (lending == null || lending.getDueDate() == null || lending.getStatus() == LendingStatus.RETURNED)
            return false;

        return lending.getDueDate().isBefore(LocalDate.now());
    }

    @Named("getDaysLeft")
    default int getDaysLeft(Lending lending) {
        if (lending == null || lending.getDueDate() == null || lending.getStatus() == LendingStatus.RETURNED)
            return 0;

        return (int)ChronoUnit.DAYS.between(LocalDate.now(), lending.getDueDate());
    }
}
