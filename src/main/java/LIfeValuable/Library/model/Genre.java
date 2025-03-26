package LifeValuable.Library.model;

import lombok.*;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
public class Genre {
    @NotNull Long id;
    @NotBlank String name;
    @NotBlank String description;
}
