package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.mapper.ReaderMapper;
import LifeValuable.Library.model.Reader;
import LifeValuable.Library.model.Role;
import LifeValuable.Library.repository.ReaderRepository;
import LifeValuable.Library.service.ReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ReaderServiceImpl implements ReaderService {
    private final ReaderRepository readerRepository;
    private final ReaderMapper readerMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ReaderServiceImpl(ReaderRepository readerRepository, ReaderMapper readerMapper, PasswordEncoder passwordEncoder) {
        this.readerRepository = readerRepository;
        this.readerMapper = readerMapper;
        this.passwordEncoder = passwordEncoder;
    }

    
    @Override
    public ReaderDetailDTO create(CreateReaderDTO createReaderDTO) {
        if (readerRepository.findByEmail(createReaderDTO.email()).isPresent())
            throw new RuntimeException("A reader with email " + createReaderDTO.email() + " already exists");

        if (!createReaderDTO.phoneNumber().isEmpty() && readerRepository.findByPhoneNumber(createReaderDTO.phoneNumber()).isPresent())
            throw new RuntimeException("A reader with phone number " + createReaderDTO.phoneNumber() + " already exists");

        Reader reader = readerMapper.toEntity(createReaderDTO);
        reader.setRegistrationDate(LocalDate.now());

        reader.setPassword(passwordEncoder.encode(createReaderDTO.password()));
        reader.setRole(Role.READER);

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

    @Transactional
    @Override
    public ReaderDetailDTO updateCurrentUser(CreateReaderDTO dto, String currentEmail) {
        Reader readerToUpdate = readerRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ReaderNotFoundException("email", currentEmail));

        if (!readerToUpdate.getEmail().equals(dto.email()) &&
                readerRepository.findByEmail(dto.email()).isPresent()) {
            throw new RuntimeException("A reader with email " + dto.email() + " already exists");
        }

        if (!dto.phoneNumber().isEmpty() &&
                !dto.phoneNumber().equals(readerToUpdate.getPhoneNumber()) &&
                readerRepository.findByPhoneNumber(dto.phoneNumber()).isPresent()) {
            throw new RuntimeException("A reader with phone number " + dto.phoneNumber() + " already exists");
        }

        readerToUpdate.setFirstName(dto.firstName());
        readerToUpdate.setLastName(dto.lastName());
        readerToUpdate.setEmail(dto.email());
        readerToUpdate.setPhoneNumber(dto.phoneNumber());

        return readerMapper.toDetailDto(readerToUpdate);
    }

    @Override
    public void deleteById(Long id) {
        if (!readerRepository.existsById(id))
            throw new ReaderNotFoundException(id);

        readerRepository.deleteById(id);
    }

    @Transactional
    @Override
    public ReaderDetailDTO findById(Long id) {
        Reader reader = readerRepository.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));
        return readerMapper.toDetailDto(reader);
    }

    @Transactional
    @Override
    public Reader findModelById(Long id) {
        return readerRepository.findById(id).orElseThrow(() -> new ReaderNotFoundException(id));
    }

    @Transactional
    @Override
    public Page<ReaderDTO> findAll(Pageable pageable) {
        return readerRepository.findAll(pageable).map(readerMapper::toDto);
    }

    @Transactional
    @Override
    public ReaderDetailDTO findByPhoneNumber(String number) {
        Reader reader = readerRepository.findByPhoneNumber(number).orElseThrow(() -> new ReaderNotFoundException("phoneNumber", number));
        return readerMapper.toDetailDto(reader);
    }

    @Transactional
    @Override
    public ReaderDetailDTO findByEmail(String email) {
        Reader reader = readerRepository.findByEmail(email).orElseThrow(() -> new ReaderNotFoundException("email", email));
        return readerMapper.toDetailDto(reader);
    }
}
