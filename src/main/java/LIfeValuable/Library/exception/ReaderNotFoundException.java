package LifeValuable.Library.exception;

public class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException(Long id) {
        super(String.format("Reader not found with id: %d", id));
    }

    public ReaderNotFoundException(String field, String value) {
        super(String.format("Reader not found with %s: %s", field, value));
    }
}
