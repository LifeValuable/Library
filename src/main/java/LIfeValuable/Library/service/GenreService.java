package LifeValuable.Library.service;

import LifeValuable.Library.dto.genre.GenreDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GenreService {
    GenreDTO findById(Long id);
    Page<GenreDTO> findAll(Pageable pageable);
    List<GenreDTO> findAll();
}
