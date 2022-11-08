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

        return films.add(film);
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

    public List<Film> getCommonsFilms(Integer userId,Integer friendId){
        log.info("Общие фильмы пользователя id = {},с другом id = {} {}",userId,friendId,films.getCommonFilms(userId, friendId));
        return films.getCommonFilms(userId,friendId)
                .stream()
                .sorted(((o1, o2) -> o2.getLikes().size() - o1.getLikes().size()))
                .collect(Collectors.toList());
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

    public List<Film> getTopFilms(Integer count, Integer genreId, Integer year) throws ResponseStatusException {
        if (count <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "count не может быть отрицательным либо равен 0");
        }
        if (year != null && year < minDate.getYear()) {
            log.warn("Год поиска не может быть раньше 1895\nТекущий год релиза: " + year);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Год поиска не может быть раньше 1895");
        }
        return films.getTopFilms(count, genreId, year);
    }

    public Film getFilm(Integer filmId) {
        if (filmId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        return films.getFilm(filmId);
    }

    public List<Film> getSortedDirectorFilms(Integer directorId, String sortBy) {
        if (directorId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id не может быть отрицательным либо равен 0");
        }
        if (!(sortBy.equals("year") || sortBy.equals("likes"))) {
            log.warn("Невозможно отсортировать по: " + sortBy);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Сортировка может быть только по year или likes");
        }
        return films.getSortedDirectorFilms(directorId, sortBy);
    }

    public void deleteFilm(Integer filmId) {
        if (filmId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        films.delete(filmId);
        log.info("Фильм с id=" + filmId + " удален");
    }
}
