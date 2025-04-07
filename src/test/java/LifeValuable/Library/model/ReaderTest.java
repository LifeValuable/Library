package LifeValuable.Library.model;

import jakarta.validation.constraints.AssertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

public class ReaderTest extends BaseModelTest<Reader> {
    @Test
    void whenReaderCreated_thenAllFieldsAreCorrectlySet() {
        Reader reader = new Reader();
        reader.setId(1L);
        reader.setFirstName("Alice");
        reader.setLastName("Black");
        reader.setEmail("alice_black@domen.com");
        reader.setPhoneNumber("+79876543210");
        reader.setRegistrationDate(LocalDate.now());
    }

    @Test
    void whenSetBlankFirstName_thenValidationFails() {
        Reader reader = new Reader();
        reader.setFirstName("");
        assertThatPropertyIsNotValid(reader, "firstName");
    }

    @Test
    void whenSetBlankLastName_thenValidationFails() {
        Reader reader = new Reader();
        reader.setLastName("");
        assertThatPropertyIsNotValid(reader, "lastName");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not_email", "not@email@too", "@not_email", "@not.email"})
    void whenSetWrongEmail_thenValidationFails(String email) {
        Reader reader = new Reader();
        reader.setEmail(email);
        assertThatPropertyIsNotValid(reader, "email");
    }

    @ParameterizedTest
    @ValueSource(strings = {"aaa", "+7(999)-999-99-99", "+7123456", "+7987654321012345", "79999999999", " "})
    void whenSetWrongPhone_thenValidationFails(String phone) {
        Reader reader = new Reader();
        reader.setPhoneNumber(phone);
        assertThatPropertyIsNotValid(reader, "phoneNumber");
    }

    @Test
    void whenSetBlankPhone_thenPropertyIsCorrectlySet() {
        Reader reader = new Reader();
        reader.setPhoneNumber("");
        assertThatPropertyIsValid(reader, "phoneNumber");
    }

    @Test
    void whenSetNullRegistrationDate_thenValidationFails() {
        Reader reader = new Reader();
        reader.setRegistrationDate(null);
        assertThatPropertyIsNotValid(reader, "registrationDate");
    }

    @Test
    void whenSetRegistrationDateInFuture_thenValidationFails() {
        Reader reader = new Reader();
        reader.setRegistrationDate(LocalDate.now().plusDays(1));
        assertThatAnnotationTriggered(reader, AssertTrue.class);
    }
}
