package LifeValuable.Library.exception;

public class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException(Long id) {
        super(String.format("Reader not found with id: %d", id));
    }
}
