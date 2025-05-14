package LifeValuable.Library.exception;

public class GenreNotFoundException extends RuntimeException {
    public GenreNotFoundException(Long id) {
        super(String.format("Genre not found with id: %d", id));
    }

    public GenreNotFoundException(String name) {
        super(String.format("Genre not found with name: %s", name));
    }
}
