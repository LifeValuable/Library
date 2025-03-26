package LifeValuable.Library.model;

import lombok.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.ISBN;

import java.util.List;

@Data
@NoArgsConstructor
public class Book {
    @NotNull private Long id;
    @NotBlank private String title;
    @NotBlank private String author;
    @ISBN private String isbn;
    private Integer publicationYear;
    @NotEmpty private List<String> genre;
    @Min(0) private Integer stock;
}
