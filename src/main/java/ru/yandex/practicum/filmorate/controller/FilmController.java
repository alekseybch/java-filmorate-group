package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Integer, Film> films = new HashMap<>();
    private final LocalDate minDate = LocalDate.of(1895,12,28);
    private Integer id = 1;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        if (films.containsValue(film)) {
            log.warn("Такой фильм уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой фильм уже есть");
        }
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        film.setId(getId());
        films.put(film.getId(), film);
        log.info("Фильм {} сохранен", film);
        return film;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film) {
        if (!containsId(film)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id=" + film.getId() + "нет");
        }
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        films.put(film.getId(), film);
        log.info("Фильм {} обновлен", film);
        return film;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilms() {
        log.info("Текущее кол-во фильмов: " +films.size());
        return new ArrayList<>(films.values());
    }

    private Integer getId() {
        return id++;
    }

    private boolean containsId(Film film) {
        for (Film savedFilm : films.values()) {
            if (savedFilm.getId() != null && savedFilm.getId().equals(film.getId())) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<String> handleException(ResponseStatusException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<String> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + exception.getFieldError().getDefaultMessage());
    }
}
