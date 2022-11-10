package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("FilmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final Date date = new Date();

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film add(Film film) throws ResponseStatusException {
        if (dbContainsFilm(film)) {
            log.warn("Такой фильм уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой фильм уже есть");
        }
        Integer filmId = addFilmInfo(film);
        film.setId(filmId);
        String sqlQuery = "INSERT INTO genre_films (film_id, genre_id) "
                + " VALUES (?, ?)";
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sqlQuery,
                        filmId,
                        genre.getId()
                );
            }
        }
        String sqlQuery2 = "INSERT INTO director_films (film_id, director_id) "
                + " VALUES (?, ?)";
        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                jdbcTemplate.update(sqlQuery2,
                        filmId,
                        director.getId()
                );
            }
        }
        log.info("Фильм {} сохранен", film);
        return getFilm(filmId);
    }

    @Override
    public Film update(Film film) {
        String sqlQuery = "UPDATE film " +
                "SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
        if (jdbcTemplate.update(sqlQuery, film.getName(), film.getDescription(), film.getReleaseDate()
                , film.getDuration(), film.getMpa().getId(), film.getId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id=" + film.getId() + " нет");
        }
        if (film.getGenres().size() == 0) {
            String sqlQuery2 = "DELETE FROM genre_films WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery2, film.getId());
        }
        if (film.getGenres() != null && film.getGenres().size() != 0) {
            String sqlQuery2 = "DELETE FROM genre_films WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery2, film.getId());
            String sqlQuery3 = "INSERT INTO genre_films (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> jdbcTemplate.update(sqlQuery3, film.getId(), genre.getId()));
        }
        if (film.getDirectors().size() == 0) {
            String sqlQuery2 = "DELETE FROM director_films WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery2, film.getId());
        }
        if (film.getDirectors() != null && film.getDirectors().size() != 0) {
            String sqlQuery2 = "DELETE FROM director_films WHERE film_id = ?";
            jdbcTemplate.update(sqlQuery2, film.getId());
            String sqlQuery3 = "INSERT INTO director_films (film_id, director_id) VALUES (?, ?)";
            film.getDirectors().forEach(director -> jdbcTemplate.update(sqlQuery3, film.getId(), director.getId()));
        }
        return getFilm(film.getId());
    }

    @Override
    public void delete(Integer filmId) {
        String sqlQuery = "DELETE FROM film WHERE film_id = ?";
        if (jdbcTemplate.update(sqlQuery, filmId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id=" + filmId + " нет");
        }
    }

    public List<Film> getFilmsList() {
        String sqlQuery = "SELECT film.*, mpa.mpa_name FROM film JOIN mpa ON film.mpa = mpa.mpa_id";
        return jdbcTemplate.query(sqlQuery, this::makeFilm);
    }

    @Override
    public List<Film> getCommonFilms(Integer userId, Integer friendId) {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса списка общих фильмов!" +
                    " Невозможно получить список фильмов несуществующего пользователя с id=" + userId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(friendId)) {
            String message = "Ошибка запроса списка общих фильмов!" +
                    " Невозможно получить список фильмов несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "SELECT f.*, m.mpa_name " +
                "FROM film AS f " +
                "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                "LEFT JOIN likes AS l on f.film_id = l.film_id " +
                "WHERE f.film_id IN (SELECT f.film_id FROM film AS f " +
                "LEFT JOIN likes lu on lu.film_id = f.film_id " +
                "LEFT JOIN likes lf on lf.film_id = f.film_id " +
                "WHERE lu.person_id = ? AND lf.person_id = ?) " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.person_id) DESC";
        return jdbcTemplate.query(sqlQuery, this::makeFilm, userId, friendId);
    }

    public List<Film> getTopFilms(Integer count, Integer genreId, Integer year) {
        if (genreId != null && !dbContainsGenre(genreId)) {
            String message = "Ошибка запроса списка популярных фильмов по жанру!" +
                    " Невозможно получить список фильмов несуществующего жанра с id=" + genreId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (genreId != null && year != null) {
            String sqlQuery = "SELECT f.*, m.mpa_name FROM film AS f " +
                    "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                    "LEFT JOIN genre_films AS gf ON f.film_id = gf.film_id " +
                    "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id " +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                    "WHERE g.genre_id = ? AND EXTRACT(YEAR FROM CAST(release_date AS date)) = ? " +
                    "GROUP BY f.film_id ORDER BY COUNT(l.person_id) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::makeFilm, genreId, year, count);
        } else if (genreId != null) {
            String sqlQuery = "SELECT f.*, m.mpa_name FROM film AS f " +
                    "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                    "LEFT JOIN genre_films AS gf ON f.film_id = gf.film_id " +
                    "LEFT JOIN genre AS g ON gf.genre_id = g.genre_id " +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                    "WHERE g.genre_id = ? " +
                    "GROUP BY f.film_id ORDER BY COUNT(l.person_id) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::makeFilm, genreId, count);
        } else if (year != null) {
            String sqlQuery = "SELECT f.*, m.mpa_name FROM film AS f " +
                    "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                    "WHERE EXTRACT(YEAR FROM CAST(release_date AS date)) = ? " +
                    "GROUP BY f.film_id ORDER BY COUNT(l.person_id) DESC LIMIT ?";
            return jdbcTemplate.query(sqlQuery, this::makeFilm, year, count);
        }
        String sqlQuery = "SELECT f.*, m.* FROM film AS f LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id ORDER BY COUNT(l.person_id) DESC LIMIT ?";
        return jdbcTemplate.query(sqlQuery, this::makeFilm, count);
    }

    @Override
    public Film getFilm(Integer id) {
        String sqlQuery = "SELECT film_id, name, description, release_date, duration, film.mpa, mpa.mpa_name " +
                "FROM film JOIN MPA ON film.mpa = mpa.mpa_id WHERE film.film_id = ?";
        Film film;
        try {
            film = jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильма с id=" + id + " нет");
        }
        return film;
    }

    @Override
    public List<Film> getSortedDirectorFilms(Integer directorId, String sortBy) {
        if (!dbContainsDirector(directorId)) {
            String message = "Ошибка запроса списка фильмов режиссера." +
                    " Невозможно получить список фильмов несуществующего режиссера с id= " + directorId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        List<Film> films = null;
        switch (sortBy) {
            case "year":
                String sqlQuery = "SELECT f.*, m.mpa_name FROM film AS f " +
                        "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                        "LEFT JOIN director_films AS df ON f.film_id = df.film_id " +
                        "LEFT JOIN director AS d ON df.director_id = d.director_id WHERE d.director_id = ? " +
                        "ORDER BY EXTRACT(YEAR FROM CAST(release_date AS date))";
                films = jdbcTemplate.query(sqlQuery, this::makeFilm, directorId);
                break;
            case "likes":
                sqlQuery = "SELECT f.*, m.mpa_name FROM film AS f " +
                        "LEFT JOIN mpa AS m ON f.mpa = m.mpa_id " +
                        "LEFT JOIN director_films AS df ON f.film_id = df.film_id " +
                        "LEFT JOIN director AS d ON df.director_id = d.director_id " +
                        "LEFT JOIN likes AS l ON f.film_id = l.film_id " +
                        "WHERE d.director_id = ? GROUP BY f.film_id " +
                        "ORDER BY COUNT(l.person_id) DESC";
                films = jdbcTemplate.query(sqlQuery, this::makeFilm, directorId);
        }
        return films;
    }

    @Override
    public void addLike(Integer userId, Integer filmId) throws ResponseStatusException {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Невозможно поставить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsFilm(filmId)) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Невозможно поставить лайк фильму с id= " + filmId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "INSERT INTO likes (person_id, film_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, userId, filmId);
            addToFeedAddLike(userId, filmId);
        } catch (DuplicateKeyException e) {
            String message = "Ошибка запроса добавления лайка фильму." +
                    " Попытка полькователем поставить лайк дважды одному фильму.";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Override
    public void deleteLike(Integer userId, Integer filmId) {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк от пользователя с id= " + userId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsFilm(filmId)) {
            String message = "Ошибка запроса удаления лайка" +
                    " Невозможно удалить лайк с фильма с id= " + filmId + " которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "DELETE FROM likes where person_id = ? AND film_id = ?";
        addToFeedDeleteLike(userId, filmId);
        if (jdbcTemplate.update(sqlQuery, userId, filmId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Лайка от пользователя с id=" + userId + " у фильма с id=" + filmId + " нет");
        }
    }

    private Film makeFilm(ResultSet resultSet, int rowSum) throws SQLException {
        Film film = Film.builder()
                .id(resultSet.getInt("film_id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .releaseDate(resultSet.getDate("release_date").toLocalDate())
                .duration(resultSet.getInt("duration"))
                .mpa(new Mpa(resultSet.getInt("mpa"), resultSet.getString("mpa_name")))
                .build();
        String sqlQuery = "SELECT person.* FROM likes JOIN person ON likes.person_id=person.person_id WHERE likes.film_id=?";
        film.getLikes().addAll(jdbcTemplate.query(sqlQuery, this::makeUser, film.getId()));
        film.getGenres().addAll(findGenresByFilmId(film.getId()));
        film.getDirectors().addAll(findDirectorsByFilmId(film.getId()));
        return film;
    }

    private Set<Director> findDirectorsByFilmId(Integer id) {
        String sqlQuery = "SELECT d.director_id, d.director_name FROM film AS f JOIN director_films AS df " +
                "ON f.film_id=df.film_id JOIN director AS d ON df.director_id=d.director_id WHERE f.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sqlQuery, this::makeDirector, id));
    }

    private int addFilmInfo(Film film) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("film")
                .usingGeneratedKeyColumns("film_id");
        return simpleJdbcInsert.executeAndReturnKey(film.toMap()).intValue();
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("person_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private Set<Genre> findGenresByFilmId(Integer id) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name FROM film AS f JOIN genre_films AS gf " +
                "ON f.film_id=gf.film_id JOIN genre AS g ON gf.genre_id=g.genre_id WHERE f.film_id = ?";
        return new TreeSet<>(jdbcTemplate.query(sqlQuery, this::makeGenre, id));
    }

    private boolean dbContainsFilm(Film film) {
        String sqlQuery = "SELECT f.*, mpa.mpa_name FROM FILM AS f JOIN mpa ON f.mpa = mpa.mpa_id " +
                "WHERE f.name = ? AND  f.description = ? AND f.release_date = ? AND f.duration = ? AND f.mpa = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, film.getName(), film.getDescription(),
                    film.getReleaseDate(), film.getDuration(), film.getMpa().getId());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsUser(Integer userId) {
        String sqlQuery = "SELECT * FROM person WHERE person_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsFilm(Integer filmId) {
        String sqlQuery = "SELECT f.*, mpa.mpa_name FROM FILM AS f JOIN mpa ON f.mpa = mpa.mpa_id " +
                "WHERE f.film_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeFilm, filmId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsGenre(Integer genreId) {
        String sqlQuery = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeGenre, genreId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsDirector(Integer directorId) {
        String sqlQuery = "SELECT * FROM director WHERE director_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeDirector, directorId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Genre makeGenre(ResultSet resultSet, int rowSum) throws SQLException {
        return new Genre(resultSet.getInt("genre_id"), resultSet.getString("genre_name"));
    }

    private Director makeDirector(ResultSet resultSet, int rowSum) throws SQLException {
        return new Director(resultSet.getInt("director_id"), resultSet.getString("director_name"));
    }

    private void addToFeedDeleteLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO feed (user_id, event_type, operation,entity_id,time_stamp)" +
                " VALUES (?, 'LIKE', 'REMOVE', ?, ?)";
        jdbcTemplate.update(sql, userId, filmId, date.getTime());
    }

    private void addToFeedAddLike(Integer userId, Integer filmId) {
        String sql = "INSERT INTO feed (user_id, event_type, operation,entity_id,time_stamp)" +
                " VALUES (?, 'LIKE', 'ADD', ?, ?)";
        jdbcTemplate.update(sql, userId, filmId, date.getTime());
    }
}
