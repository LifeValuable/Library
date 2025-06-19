package LifeValuable.Library.repository;

import LifeValuable.Library.config.DataConfig;
import LifeValuable.Library.model.Reader;
import LifeValuable.Library.model.Role;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(DataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class ReaderRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ReaderRepository readerRepository;

    @BeforeEach
    void setUp() {
        entityManager.createQuery("DELETE FROM Reader").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE reader ALTER COLUMN id RESTART WITH 1").executeUpdate();

        Reader reader1 = new Reader();
        reader1.setFirstName("Alice");
        reader1.setLastName("Black");
        reader1.setEmail("alice_black@domen.com");
        reader1.setPhoneNumber("+79876543210");
        reader1.setRegistrationDate(LocalDate.now());
        reader1.setPassword("password");
        reader1.setRole(Role.READER);

        Reader reader2 = new Reader();
        reader2.setFirstName("Vasya");
        reader2.setLastName("White");
        reader2.setEmail("white@vasya.ru");
        reader2.setPhoneNumber("+70123456789");
        reader2.setRegistrationDate(LocalDate.now().minusDays(1));
        reader2.setPassword("password");
        reader2.setRole(Role.READER);

        entityManager.persist(reader1);
        entityManager.persist(reader2);
        entityManager.flush();
    }

    @Test
    public void whenFindReaderById_thenReaderIsFound() {
        Optional<Reader> reader = readerRepository.findById(1L);

        assertThat(reader)
                .isPresent();
        assertThat(reader.get().getEmail()).isEqualTo("alice_black@domen.com");
    }

    @Test
    public void whenFindReaderById_thenReaderIsNotFound() {
        Optional<Reader> reader = readerRepository.findById(3L);

        assertThat(reader)
                .isEmpty();
    }

    @Test
    public void whenSaveReader_thenReaderIsSaved() {
        Reader reader = new Reader();
        reader.setFirstName("Bob");
        reader.setLastName("Bobina");
        reader.setEmail("gubka@bob.com");
        reader.setPhoneNumber("+71234567890");
        reader.setRegistrationDate(LocalDate.now());
        reader.setPassword("password");
        reader.setRole(Role.READER);

        Reader savedReader = readerRepository.save(reader);
        assertThat(savedReader.getId()).isNotNull();

        Optional<Reader> foundReader = readerRepository.findById(savedReader.getId());
        assertThat(foundReader).isPresent();
        assertThat(foundReader.get()).isEqualTo(savedReader);
    }

    @Test
    public void whenUpdateReaderById_thenReaderUpdated() {
        Optional<Reader> readerToUpdate = readerRepository.findById(1L);
        assertThat(readerToUpdate).isPresent();

        Reader reader = readerToUpdate.get();
        reader.setFirstName("NewName");

        Reader savedReader = readerRepository.save(reader);
        assertThat(savedReader).isEqualTo(reader);

        Optional<Reader> foundReader = readerRepository.findById(1L);
        assertThat(foundReader).isPresent();
        assertThat(foundReader.get()).isEqualTo(savedReader);
    }

    @Test 
    public void whenDeleteReaderById_thenReaderIsRemoved() {
        assertThat(readerRepository.findById(1L)).isPresent();

        readerRepository.deleteById(1L);

        assertThat(readerRepository.findById(1L)).isEmpty();
    }
    
    @Test
    public void whenDeleteReader_thenReaderIsRemoved() {
        Optional<Reader> reader = readerRepository.findById(1L);
        assertThat(reader).isPresent();

        readerRepository.delete(reader.get());

        assertThat(readerRepository.findById(1L)).isEmpty();
    }
    
    @Test
    public void whenFindAllReaders_thenAllReadersAreReturned() {
        List<Reader> readers = readerRepository.findAll();

        assertThat(readers).hasSize(2);
    }
    
    @Test
    public void whenDeleteAllReaders_thenNoReadersAreFound() {
        assertThat(readerRepository.findAll()).isNotEmpty();

        readerRepository.deleteAll();

        assertThat(readerRepository.findAll()).isEmpty();
    }
    
    @Test
    void whenCountReaders_thenCorrectNumberReturned() {
        long count = readerRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void whenFindFindReaderByPhoneNumber_thenReaderIsFound() {
        String number = "+79876543210";
        Optional<Reader> reader = readerRepository.findByPhoneNumber(number);

        assertThat(reader)
                .isPresent();
        assertThat(reader.get().getPhoneNumber())
                .isEqualTo(number);
    }

    @ValueSource(strings = {"", "+79876543211", "79876543210", "89876543210"})
    @ParameterizedTest
    void whenFindFindReaderByPhoneNumber_thenReaderIsNotFound(String number) {
        Optional<Reader> reader = readerRepository.findByPhoneNumber(number);

        assertThat(reader)
                .isEmpty();
    }

    @Test
    void whenFindFindReaderByEmail_thenReaderIsFound() {
        String email = "white@vasya.ru";
        Optional<Reader> reader = readerRepository.findByEmail(email);

        assertThat(reader)
                .isPresent();
        assertThat(reader.get().getEmail())
                .isEqualTo(email);
    }

    @ValueSource(strings = {"", " white@vasya.ru ", "white@vasya_ru"})
    @ParameterizedTest
    void whenFindFindReaderByEmail_thenReaderIsNotFound(String email) {
        Optional<Reader> reader = readerRepository.findByEmail(email);

        assertThat(reader)
                .isEmpty();
    }
}
