package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @PutMapping("{id}/like/{userId}")
    public void addLike(@PathVariable Integer userId, @PathVariable("id") Integer filmId) {
        filmService.addLike(userId, filmId);
    }

    @GetMapping("{id}")
    public Film getFilm(@PathVariable("id") Integer filmId) {
        return filmService.getFilm(filmId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer userId, @PathVariable("id") Integer filmId) {
        filmService.deleteLike(userId,filmId);
    }

    @GetMapping("popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count,
                                      @RequestParam(required = false) Integer genreId,
                                      @RequestParam(required = false) Integer year) {
        return filmService.getTopFilms(count, genreId, year);
    }

    @GetMapping("director/{directorId}")
    public List<Film> getDirectorFilms(@PathVariable Integer directorId,
                                       @RequestParam(defaultValue = "likes") String sortBy) {
        return filmService.getSortedDirectorFilms(directorId, sortBy);
    }

    @DeleteMapping("{id}")
    public void deleteFilm(@PathVariable("id") Integer filmId) {
        filmService.deleteFilm(filmId);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "by", required = false) String by) {
        return filmService.searchFilms(query, by);
    }
}
