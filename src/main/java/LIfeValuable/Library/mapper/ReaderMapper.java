package LifeValuable.Library.mapper;

import LifeValuable.Library.dto.reader.*;
import LifeValuable.Library.model.LendingStatus;
import LifeValuable.Library.model.Reader;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ReaderMapper {

    @Mapping(target = "fullName", source = "reader", qualifiedByName = "getFullName")
    @Mapping(target = "activeLendingsCount", source = "reader", qualifiedByName = "countActiveLendings")
    ReaderDTO toDto(Reader reader);

    @Mapping(target = "activeLendingsCount", source = "reader", qualifiedByName = "countActiveLendings")
    @Mapping(target = "overdueLendingsCount", source = "reader", qualifiedByName = "countOverdueLendings")
    @Mapping(target = "totalBorrowed", source = "reader", qualifiedByName = "countTotalLendings")
    ReaderDetailDTO toDetailDto(Reader reader);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDate", source = "createReaderDTO", qualifiedByName = "getCurrentDate")
    @Mapping(target = "lendings", ignore = true)
    Reader toEntity(CreateReaderDTO createReaderDTO);

    @Named("getFullName")
    default String getFullName(Reader reader) {
        if (reader == null)
            return "";

        return reader.getFirstName() + " " + reader.getLastName();
    }

    @Named("countActiveLendings")
    default int countActiveLendings(Reader reader) {
        if (reader == null || reader.getLendings() == null)
            return 0;

        return (int) reader.getLendings().stream()
                .filter(l -> l.getStatus() == LendingStatus.ACTIVE)
                .count();
    }

    @Named("countOverdueLendings")
    default int countOverdueLendings(Reader reader) {
        if (reader == null || reader.getLendings() == null)
            return 0;

        return (int) reader.getLendings().stream()
                .filter(l -> l.getStatus() == LendingStatus.OVERDUE)
                .count();
    }

    @Named("countTotalLendings")
    default int countTotalLendings(Reader reader) {
        if (reader == null || reader.getLendings() == null)
            return 0;
        return reader.getLendings().size();
    }

    @Named("getCurrentDate")
    default LocalDate getCurrentDate(CreateReaderDTO dto) {
        return LocalDate.now();
    }

    List<ReaderDTO> toDtoList(List<Reader> readers);
}

