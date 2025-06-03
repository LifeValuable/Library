package LifeValuable.Library.exception;

@lombok.Getter
public class LendingNotFoundException extends RuntimeException {
    private final String apiMessage;

    public LendingNotFoundException(Long id) {
        super("Lending not found with id: " + id);
        apiMessage = "Не найдена выдача с id: " + id;
    }
}
