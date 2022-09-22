package ru.yandex.practicum.filmorate.model;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
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
    private final Set<Integer> likes = new HashSet<>();

    public void addLike(User user) throws ResponseStatusException {
        if (likes.contains(user.getId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR
                    , "Невозможно дважды поставить лайк фильм одному и тому же фильму.");
        }
        likes.add(user.getId());
    }

    public void deleteLike(User user) {
        if (!likes.contains(user.getId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR
                    , "Невозможно удалить лайк у фильма от пользователя который его не ставил.");
        }
        likes.remove(user.getId());
    }
}
