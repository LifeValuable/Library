package LifeValuable.Library.model;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.ISBN;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String title;
    @NotBlank private String author;
    @ISBN private String isbn;
    private Integer publicationYear;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "book_genre",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @NotEmpty private List<Genre> genres;
    @Min(0) private Integer stock;

    public void setPublicationYear(Integer year) {
        if (year > LocalDate.now().getYear())
            throw new IllegalArgumentException("Publication year can't be in future");
        if (year < 0)
            throw new IllegalArgumentException("Publication year can't be negative");

        this.publicationYear = year;
    }
}
