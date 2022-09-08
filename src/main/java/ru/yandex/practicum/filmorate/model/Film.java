package ru.yandex.practicum.filmorate.model;

import lombok.*;


import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    @EqualsAndHashCode.Exclude
    private Integer id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @Size(max = 200, message = "Максимальная длина описания 200 символов")
    private String description;
    @PastOrPresent(message = "Некорректная дата релиза")
    private LocalDate releaseDate;
    @Positive(message = "Некорректная продолжительность фильма")
    private Long duration;
}
