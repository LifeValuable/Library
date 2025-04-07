package LifeValuable.Library.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import LifeValuable.Library.config.DataConfig;
import LifeValuable.Library.model.Genre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringJUnitConfig(DataConfig.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class GenreRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private GenreRepository genreRepository;

    @BeforeEach
    public void setUp() {
        entityManager.createQuery("DELETE FROM Genre").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE genre ALTER COLUMN id RESTART WITH 1").executeUpdate();

        Genre genre1 = new Genre();
        genre1.setName("Фантастика");
        genre1.setDescription("Что-то невероятное");
        Genre genre2 = new Genre();
        genre2.setName("Боевик");
        genre2.setDescription("Много пушек");

        entityManager.persist(genre1);
        entityManager.persist(genre2);
        entityManager.flush();
    }

    @Test
    public void whenFindGenreById_thenGenreIsFound() {
        Optional<Genre> foundGenre = genreRepository.findById(1L);

        assertThat(foundGenre)
                .isPresent();
        assertThat(foundGenre.get().getName()).isEqualTo("Фантастика");
    }

    @Test
    public void whenFindGenreByName_thenGenreIsFound() {
        Optional<Genre> foundGenre = genreRepository.findByName("Фантастика");

        assertThat(foundGenre)
                .isPresent();
        assertThat(foundGenre.get().getName()).isEqualTo("Фантастика");
    }

    @Test
    public void whenFindGenreById_thenGenreIsNotFound() {
        Optional<Genre> foundGenre = genreRepository.findById(3L);

        assertThat(foundGenre)
                .isEmpty();
    }

    @Test
    public void whenFindGenreByName_thenGenreIsNotFound() {
        Optional<Genre> foundGenre = genreRepository.findByName("абракадабра");

        assertThat(foundGenre)
                .isEmpty();
    }

    @Test
    public void whenSaveGenre_thenGenreIsSaved() {
        Genre newGenre = new Genre();
        newGenre.setName("Детектив");
        newGenre.setDescription("Расследование загадочных происшествий");

        Genre savedGenre = genreRepository.save(newGenre);

        assertThat(savedGenre.getId()).isNotNull();

        Optional<Genre> foundGenre = genreRepository.findById(savedGenre.getId());
        assertThat(foundGenre).isPresent();
        assertThat(foundGenre.get().getName()).isEqualTo("Детектив");
    }

    @Test
    public void whenUpdateGenre_thenGenreIsUpdated() {
        Optional<Genre> genreToUpdate = genreRepository.findById(1L);
        assertThat(genreToUpdate).isPresent();

        Genre genre = genreToUpdate.get();
        genre.setName("Научная фантастика");
        genre.setDescription("Научно обоснованные фантастические идеи");

        Genre updatedGenre = genreRepository.save(genre);

        assertThat(updatedGenre).isEqualTo(genre);

        Optional<Genre> foundGenre = genreRepository.findById(1L);
        assertThat(foundGenre).isPresent();
        assertThat(foundGenre.get()).isEqualTo(updatedGenre);
    }

    @Test
    public void whenDeleteGenreById_thenGenreIsRemoved() {
        assertThat(genreRepository.findById(1L)).isPresent();

        genreRepository.deleteById(1L);

        assertThat(genreRepository.findById(1L)).isEmpty();
    }

    @Test
    public void whenDeleteGenre_thenGenreIsRemoved() {
        Optional<Genre> genreToDelete = genreRepository.findById(1L);
        assertThat(genreToDelete).isPresent();

        genreRepository.delete(genreToDelete.get());

        assertThat(genreRepository.findById(1L)).isEmpty();
    }

    @Test
    public void whenFindAllGenres_thenAllGenresAreReturned() {
        Iterable<Genre> genres = genreRepository.findAll();

        assertThat(genres).hasSize(2);
    }

    @Test
    public void whenDeleteAllGenres_thenNoGenresAreFound() {
        assertThat(genreRepository.findAll()).isNotEmpty();

        genreRepository.deleteAll();

        assertThat(genreRepository.findAll()).isEmpty();
    }

    @Test
    public void whenCountGenres_thenCorrectNumberReturned() {
        long count = genreRepository.count();

        assertThat(count).isEqualTo(2);
    }
}
