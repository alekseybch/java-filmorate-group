package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private FilmStorage films;
    private UserStorage users;
    private final LocalDate minDate = LocalDate.of(1895, 12, 28);
    private Integer globalId = 1;

    @Autowired
    public FilmService(FilmStorage films, UserStorage users) {
        this.films = films;
        this.users = users;
    }

    public Film addFilm(Film film) throws ResponseStatusException {
        if (films.getFilmsMap().containsValue(film)) {
            log.warn("Такой фильм уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой фильм уже есть");
        }
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        film.setId(getNextId());
        films.add(film);
        log.info("Фильм {} сохранен", film);
        return film;
    }

    public Film updateFilm(Film film) throws ResponseStatusException {
        if (films.getFilmsMap().get(film.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id=" + film.getId() + " нет");
        }
        if (film.getReleaseDate().isBefore(minDate)) {
            log.warn("Дата релиза не может быть раньше 28.12.1895\nТекущая дата релиза: " + film.getReleaseDate());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Дата релиза не может быть раньше 28.12.1895");
        }
        films.update(film);
        log.info("Фильм {} обновлен", film);
        return film;
    }

    public List<Film> getFilms() {
        log.info("Текущее кол-во фильмов: " + films.getFilmsMap().size());
        return new ArrayList<>(films.getFilmsMap().values());
    }

    public String addLike(Integer userId, Integer filmId) throws ResponseStatusException {
        User user;
        Film film;
        if (users.getUsersMap().get(userId) == null) {
            String message = "Ошибка запроса добавления лайка фильму" +
                    " Невозможно поставить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            user = users.getUsersMap().get(userId);
        }
        if (films.getFilmsMap().get(filmId) == null) {
            String message = "Ошибка запроса добавления лайка фильму" +
                    " Невозможно поставить лайк фильму с id= " + filmId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            film = films.getFilmsMap().get(filmId);
        }
        film.addLike(user);
        return "Пользователь " + user.getName() + "поставил лайк фильму " + film.getName();
    }

    public String deleteLike(Integer userId, Integer filmId) throws ResponseStatusException {
        User user;
        Film film;
        if (users.getUsersMap().get(userId) == null) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            user = users.getUsersMap().get(userId);
        }
        if (films.getFilmsMap().get(filmId) == null) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк с фильма с id= " + filmId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            film = films.getFilmsMap().get(filmId);
        }
        film.deleteLike(user);
        return "Пользователь " + user.getName() + "удалил лайк с фильма " + film.getName();
    }

    public List<Film> getSortedFilms(Integer count) throws ResponseStatusException {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR
                    , "Параметр count не может быть меньше либо равен 0");
        }
        Comparator<Film> sortFilm = (f1, f2) -> {
            Integer filmLikes1 = f1.getLikes().size();
            Integer filmLikes2 = f2.getLikes().size();
            return -1 * filmLikes1.compareTo(filmLikes2);

        };
        return films.getFilmsMap().values().stream().sorted(sortFilm).limit(count)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public Film getFilm(Integer filmId) {
        if (films.getFilmsMap().get(filmId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id= " + filmId + " не существует");
        }
        return films.getFilmsMap().get(filmId);
    }

    private Integer getNextId() {
        return globalId++;
    }
}
