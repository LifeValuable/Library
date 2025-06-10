package LifeValuable.Library.service;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.model.Reader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReaderService {
    ReaderDetailDTO create(CreateReaderDTO createReaderDTO);
    ReaderDetailDTO update(CreateReaderDTO createReaderDTO, Long id);
    void deleteById(Long id);
    ReaderDetailDTO findById(Long id);
    Reader findModelById(Long id);
    Page<ReaderDTO> findAll(Pageable pageable);

    ReaderDetailDTO findByPhoneNumber(String number);
    ReaderDetailDTO findByEmail(String email);
}
