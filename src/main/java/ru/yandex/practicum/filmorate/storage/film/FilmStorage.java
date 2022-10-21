package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    void add(Film film);

    Film update(Film film);

    List<Film> getFilmsList();

    void addLike(Integer userId, Integer filmId);

    void deleteLike(Integer userId, Integer filmId);

    Film getFilm (Integer id);
}
