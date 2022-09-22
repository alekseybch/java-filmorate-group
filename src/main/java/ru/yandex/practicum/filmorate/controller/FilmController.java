package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/films")
public class FilmController {
    private FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

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
    public String addLike(@PathVariable Integer userId, @PathVariable("id") Integer filmId) {
        return filmService.addLike(userId, filmId);
    }

    @GetMapping("{id}")
    public Film getFilm(@PathVariable("id") Integer filmId) {
        return filmService.getFilm(filmId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public String deleteFilm(@PathVariable Integer userId, @PathVariable("id") Integer filmId) {
        return filmService.deleteLike(userId,filmId);
    }

    @GetMapping("popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") Integer count) {
        return filmService.getSortedFilms(count);
    }
}
