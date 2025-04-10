package LifeValuable.Library.dto.reader;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CreateReaderDTOTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenAllFieldsValid_thenValidationPasses() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Иван",
            "Иванов",
            "ivan.ivanov@example.com",
            "+79991234567"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenFirstNameIsBlank_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "",
            "Петров",
            "petr.petrov@example.com",
            "+79991234568"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenLastNameIsBlank_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Анна",
            "",
            "anna@example.com",
            "+79991234569"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenEmailIsInvalid_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Сергей",
            "Сидоров",
            "неверный-email",
            "+79991234570"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenPhoneNumberIsInvalid_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Мария",
            "Кузнецова",
            "maria@example.com",
            "неверный-телефон"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenPhoneNumberIsTooShort_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Алексей",
            "Смирнов",
            "alex@example.com",
            "+123"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void whenPhoneNumberIsTooLong_thenValidationFails() {
        CreateReaderDTO dto = new CreateReaderDTO(
            "Екатерина",
            "Николаева",
            "katya@example.com",
            "+7999123456789012345"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
    }
} 