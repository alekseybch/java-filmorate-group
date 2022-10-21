package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage films;
    private final LocalDate minDate = LocalDate.of(1895, 12, 28);

    @Autowired

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage films) {
        this.films = films;
    }

    public Film addFilm(Film film) throws ResponseStatusException {
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        films.add(film);
        log.info("Фильм {} сохранен", film);
        return film;
    }

    public Film updateFilm(Film film) throws ResponseStatusException {
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        log.info("Фильм {} обновлен", film);
        return films.update(film);
    }

    public List<Film> getFilms() {
        log.info("Текущее кол-во фильмов: " + films.getFilmsList().size());
        return films.getFilmsList();
    }

    public void addLike(Integer userId, Integer filmId) throws ResponseStatusException {
        if (userId <=0 || filmId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и filmId не могут быть отрицательныи либо равены 0");
        }
        films.addLike(userId, filmId);
        log.info("Пользователь c id = " + userId + " поставил лайк фильму c id = " + filmId);
    }

    public void deleteLike(Integer userId, Integer filmId) throws ResponseStatusException {
        if (userId <=0 || filmId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и filmId не могут быть отрицательныи либо равены 0");
        }
        films.deleteLike(userId, filmId);
        log.info("Пользователь c id=" + userId + " удалил лайк с фильма id= " + filmId);
    }

    public List<Film> getSortedFilms(Integer count) throws ResponseStatusException {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "count не может быть отрицательным либо равен 0");
        }
        Comparator<Film> sortFilm = (f1, f2) -> {
            Integer filmLikes1 = f1.getLikes().size();
            Integer filmLikes2 = f2.getLikes().size();
            return -1 * filmLikes1.compareTo(filmLikes2);

        };
        return films.getFilmsList().stream().sorted(sortFilm).limit(count)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Film getFilm(Integer filmId) {
        if (filmId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id не может быть отрицательным либо равен 0");
        }
        return films.getFilm(filmId);
    }
}
