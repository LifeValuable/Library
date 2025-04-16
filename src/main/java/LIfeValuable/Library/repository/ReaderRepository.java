package LifeValuable.Library.repository;

import LifeValuable.Library.model.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReaderRepository extends JpaRepository<Reader, Long> {
    Optional<Reader> findByEmail(String email);
    Optional<Reader> findByPhoneNumber(String number);
}
