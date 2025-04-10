package LifeValuable.Library.mapper;

import LifeValuable.Library.model.Genre;
import LifeValuable.Library.dto.genre.GenreDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    GenreDTO toDto(Genre genre);
}
