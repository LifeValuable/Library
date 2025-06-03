package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.mapper.ReaderMapper;
import LifeValuable.Library.model.Reader;
import LifeValuable.Library.repository.ReaderRepository;
import LifeValuable.Library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReaderServiceImpl implements ReaderService {
    private ReaderRepository readerRepository;
    private ReaderMapper readerMapper;

    @Autowired
    public ReaderServiceImpl(ReaderRepository readerRepository, ReaderMapper readerMapper) {
        this.readerRepository = readerRepository;
        this.readerMapper = readerMapper;
    }

    
    @Override
    public ReaderDetailDTO create(CreateReaderDTO createReaderDTO) {
        if (readerRepository.findByEmail(createReaderDTO.email()).isPresent())
            throw new RuntimeException("A reader with email " + createReaderDTO.email() + " already exists");

        if (!createReaderDTO.phoneNumber().isEmpty() && readerRepository.findByPhoneNumber(createReaderDTO.phoneNumber()).isPresent())
            throw new RuntimeException("A reader with phone number " + createReaderDTO.phoneNumber() + " already exists");

        Reader reader = readerMapper.toEntity(createReaderDTO);
        reader.setRegistrationDate(LocalDate.now());
        Reader savedReader = readerRepository.save(reader);
        return readerMapper.toDetailDto(savedReader);
    }

    @Transactional
    @Override
    public ReaderDetailDTO update(CreateReaderDTO createReaderDTO, Long id) {
        Reader readerToUpdate = readerRepository.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));

        if (readerRepository.findByEmail(createReaderDTO.email()).isPresent())
            throw new RuntimeException("A reader with email " + createReaderDTO.email() + " already exists");

        if (!createReaderDTO.phoneNumber().isEmpty() && readerRepository.findByPhoneNumber(createReaderDTO.phoneNumber()).isPresent())
            throw new RuntimeException("A reader with phone number " + createReaderDTO.phoneNumber() + " already exists");

        readerToUpdate.setFirstName(createReaderDTO.firstName());
        readerToUpdate.setLastName(createReaderDTO.lastName());
        readerToUpdate.setEmail(createReaderDTO.email());
        readerToUpdate.setPhoneNumber(createReaderDTO.phoneNumber());

        return readerMapper.toDetailDto(readerToUpdate);
    }

    @Override
    public void deleteById(Long id) {
        if (!readerRepository.existsById(id))
            throw new ReaderNotFoundException(id);

        readerRepository.deleteById(id);
    }

    @Override
    public ReaderDetailDTO findById(Long id) {
        Reader reader = readerRepository.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));
        return readerMapper.toDetailDto(reader);
    }

    @Override
    public Page<ReaderDTO> findAll(Pageable pageable) {
        return readerRepository.findAll(pageable).map(readerMapper::toDto);
    }

    @Override
    public ReaderDetailDTO findByPhoneNumber(String number) {
        Reader reader = readerRepository.findByPhoneNumber(number).orElseThrow(() -> new ReaderNotFoundException("phoneNumber", number));
        return readerMapper.toDetailDto(reader);
    }

    @Override
    public ReaderDetailDTO findByEmail(String email) {
        Reader reader = readerRepository.findByEmail(email).orElseThrow(() -> new ReaderNotFoundException("email", email));
        return readerMapper.toDetailDto(reader);
    }
}
