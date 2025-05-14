package LifeValuable.Library.exception;

public class LendingNotFoundException extends RuntimeException {
    public LendingNotFoundException(Long id) {
        super(String.format("Lending not found with id: %d", id));
    }
}
