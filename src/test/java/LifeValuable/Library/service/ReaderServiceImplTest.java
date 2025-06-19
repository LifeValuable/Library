package LifeValuable.Library.service.impl;

import LifeValuable.Library.dto.reader.CreateReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDTO;
import LifeValuable.Library.dto.reader.ReaderDetailDTO;
import LifeValuable.Library.exception.ReaderNotFoundException;
import LifeValuable.Library.mapper.ReaderMapper;
import LifeValuable.Library.model.Reader;
import LifeValuable.Library.repository.ReaderRepository;
import LifeValuable.Library.security.PasswordConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReaderServiceImplTest {

    @Mock
    private ReaderRepository readerRepository;

    private final PasswordEncoder passwordEncoder = new PasswordConfig().passwordEncoder();

    private final ReaderMapper readerMapper = Mappers.getMapper(ReaderMapper.class);

    private ReaderServiceImpl readerService;

    private Reader reader;
    private ReaderDetailDTO expectedReaderDetailDTO;
    private CreateReaderDTO createReaderDTO;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        reader = new Reader();
        reader.setId(1L);
        reader.setFirstName("Иван");
        reader.setLastName("Петров");
        reader.setEmail("ivan.p@example.com");
        reader.setPhoneNumber("+79001112233");
        reader.setRegistrationDate(today.minusDays(10));
        reader.setLendings(Collections.emptyList());

        expectedReaderDetailDTO = readerMapper.toDetailDto(reader);

        createReaderDTO = new CreateReaderDTO(
                "Сергей", "Иванов", "sergey.i@example.com", "+79112223344", "password"
        );

        readerService = new ReaderServiceImpl(readerRepository, readerMapper, passwordEncoder);

    }

    @Test
    void whenCreate_thenSaveAndReturnReaderDetailDTO() {
        Reader readerToSave = readerMapper.toEntity(createReaderDTO);
        Long expectedGeneratedId = 2L;
        readerToSave.setId(expectedGeneratedId);

        when(readerRepository.save(any(Reader.class))).thenReturn(readerToSave);

        ReaderDetailDTO expectedDto = readerMapper.toDetailDto(readerToSave);

        ReaderDetailDTO actualDto = readerService.create(createReaderDTO);

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(readerRepository).save(argThat(argument ->
                argument.getFirstName().equals(createReaderDTO.firstName()) &&
                        argument.getEmail().equals(createReaderDTO.email()) &&
                        argument.getId() == null
        ));
    }

    @Test
    void whenUpdate_withExistingId_thenUpdateAndReturnReaderDetailDTO() {
        Reader finalStateReader = new Reader();
        finalStateReader.setId(reader.getId());
        finalStateReader.setFirstName(createReaderDTO.firstName());
        finalStateReader.setLastName(createReaderDTO.lastName());
        finalStateReader.setEmail(createReaderDTO.email());
        finalStateReader.setPhoneNumber(createReaderDTO.phoneNumber());
        finalStateReader.setRegistrationDate(reader.getRegistrationDate());
        finalStateReader.setLendings(reader.getLendings());

        ReaderDetailDTO expectedDto = readerMapper.toDetailDto(finalStateReader);

        when(readerRepository.findById(reader.getId())).thenReturn(Optional.of(reader));

        ReaderDetailDTO actualDto = readerService.update(createReaderDTO, reader.getId());

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(readerRepository).findById(reader.getId());
        verify(readerRepository, never()).save(any());
    }

    @Test
    void whenUpdate_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(readerRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readerService.update(createReaderDTO, nonExistingId))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessageContaining("Reader not found with id: " + nonExistingId);

        verify(readerRepository).findById(nonExistingId);
        verify(readerRepository, never()).save(any());
    }

    @Test
    void whenDeleteById_withExistingId_thenCallRepositoryDelete() {
        when(readerRepository.existsById(reader.getId())).thenReturn(true);
        doNothing().when(readerRepository).deleteById(reader.getId());

        readerService.deleteById(reader.getId());

        verify(readerRepository).existsById(reader.getId());
        verify(readerRepository).deleteById(reader.getId());
    }

    @Test
    void whenDeleteById_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(readerRepository.existsById(nonExistingId)).thenReturn(false);

        assertThatThrownBy(() -> readerService.deleteById(nonExistingId))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessageContaining("Reader not found with id: " + nonExistingId);

        verify(readerRepository).existsById(nonExistingId);
        verify(readerRepository, never()).deleteById(anyLong());
    }

    @Test
    void whenFindById_withExistingId_thenReturnReaderDetailDTO() {
        when(readerRepository.findById(reader.getId())).thenReturn(Optional.of(reader));
        ReaderDetailDTO expectedDto = expectedReaderDetailDTO;

        ReaderDetailDTO actualDto = readerService.findById(reader.getId());

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(readerRepository).findById(reader.getId());
    }

    @Test
    void whenFindById_withNonExistingId_thenThrowNotFoundException() {
        Long nonExistingId = 99L;
        when(readerRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readerService.findById(nonExistingId))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessageContaining("Reader not found with id: " + nonExistingId);

        verify(readerRepository).findById(nonExistingId);
    }

    @Test
    void whenFindAll_thenReturnPageOfReaderDTO() {
        Pageable pageable = PageRequest.of(0, 5);
        List<Reader> readersList = List.of(reader);
        Page<Reader> readerPage = new PageImpl<>(readersList, pageable, 1);
        ReaderDTO expectedReaderDto = readerMapper.toDto(reader);

        when(readerRepository.findAll(pageable)).thenReturn(readerPage);

        Page<ReaderDTO> actualPage = readerService.findAll(pageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getTotalElements()).isEqualTo(1);
        assertThat(actualPage.getContent()).hasSize(1);
        assertThat(actualPage.getContent().get(0)).isEqualTo(expectedReaderDto);

        verify(readerRepository).findAll(pageable);
    }

    @Test
    void whenFindAll_withNoReaders_thenReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Reader> emptyPage = Page.empty(pageable);

        when(readerRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ReaderDTO> actualPage = readerService.findAll(pageable);

        assertThat(actualPage).isNotNull();
        assertThat(actualPage.isEmpty()).isTrue();
        assertThat(actualPage.getTotalElements()).isZero();

        verify(readerRepository).findAll(pageable);
    }

    @Test
    void whenFindByPhoneNumber_withExistingNumber_thenReturnReaderDetailDTO() {
        String phoneNumber = reader.getPhoneNumber();
        when(readerRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.of(reader));
        ReaderDetailDTO expectedDto = expectedReaderDetailDTO;

        ReaderDetailDTO actualDto = readerService.findByPhoneNumber(phoneNumber);

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(readerRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void whenFindByPhoneNumber_withNonExistingNumber_thenThrowNotFoundException() {
        String phoneNumber = "+70000000000";
        when(readerRepository.findByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readerService.findByPhoneNumber(phoneNumber))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessageContaining("Reader not found with phoneNumber: " + phoneNumber);

        verify(readerRepository).findByPhoneNumber(phoneNumber);
    }

    @Test
    void whenFindByEmail_withExistingEmail_thenReturnReaderDetailDTO() {
        String email = reader.getEmail();
        when(readerRepository.findByEmail(email)).thenReturn(Optional.of(reader));
        ReaderDetailDTO expectedDto = expectedReaderDetailDTO;

        ReaderDetailDTO actualDto = readerService.findByEmail(email);

        assertThat(actualDto).isEqualTo(expectedDto);
        verify(readerRepository).findByEmail(email);
    }

    @Test
    void whenFindByEmail_withNonExistingEmail_thenThrowNotFoundException() {
        String email = "not.found@example.com";
        when(readerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readerService.findByEmail(email))
                .isInstanceOf(ReaderNotFoundException.class)
                .hasMessageContaining("Reader not found with email: " + email);

        verify(readerRepository).findByEmail(email);
    }
}
