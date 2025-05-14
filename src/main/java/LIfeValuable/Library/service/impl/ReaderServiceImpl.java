package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.mapper.ReaderMapper;
import LifeValuable.Library.repository.ReaderRepository;
import LifeValuable.Library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

public class ReaderServiceImpl /*implements ReaderService*/ {
    private ReaderRepository readerRepository;
    private ReaderMapper readerMapper;

    @Autowired
    public ReaderServiceImpl(ReaderRepository readerRepository, ReaderMapper readerMapper) {
        this.readerRepository = readerRepository;
        this.readerMapper = readerMapper;
    }

//    public ReaderDetailDTO create(CreateReaderDTO createReaderDTO);
//    public ReaderDetailDTO update(CreateReaderDTO createReaderDTO, Long id);
//    public void deleteById(Long id);
//    public ReaderDetailDTO findById(Long id);
//    public Page<ReaderDTO> findAll(Pageable pageable);
//
//    public ReaderDetailDTO findByPhoneNumber(String number);
//    public ReaderDetailDTO findByEmail(String email);
}
