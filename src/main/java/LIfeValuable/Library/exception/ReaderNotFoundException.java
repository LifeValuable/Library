package LifeValuable.Library.exception;

@lombok.Getter
public class ReaderNotFoundException extends RuntimeException {
    private final String apiMessage;
    
    public ReaderNotFoundException(Long id) {
        super("Reader not found with id: " + id);
        apiMessage = "Не найден читатель с id: " + id;
    }

    public ReaderNotFoundException(String field, String value) {
        super("Reader not found with " + field + ": " + value);
        String ruField = field;
        if (field.equals("email"))
            ruField = "почтой";
        else if (ruField.equals("phoneNumber"))
            ruField = "телефоном";

        apiMessage = "Не найден читатель с " + ruField + ": " + value;
    }
}
