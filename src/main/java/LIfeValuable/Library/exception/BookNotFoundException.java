package LifeValuable.Library.exception;

@lombok.Getter
public class BookNotFoundException extends RuntimeException {
    private final String apiMessage;

    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
        apiMessage = "Не найдена книга с id: " + id;
    }
}
