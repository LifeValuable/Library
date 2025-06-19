package LifeValuable.Library.model;

import lombok.Getter;

@Getter
public enum Role {
    READER("Читатель"),
    LIBRARIAN("Библиотекарь"),
    ADMIN("Администратор");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

}
