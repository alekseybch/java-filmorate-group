package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final Date date = new Date();

    @Override
    public Review addReview(Review review) {
        if (!dbContainsFilm(review.getFilmId())) {
            String message = "Невозможно создать отзыв к фильму, которого не существует";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(review.getUserId())) {
            String message = "Невозможно создать отзыв от пользователя, которого не существует";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int reviewId = simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue();
        review.setReviewId(reviewId);
        addToFeedReviewCreate(review.getReviewId(), review.getUserId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        if (!dbContainsReview(review.getReviewId())) {
            String message = "Ошибка запроса обновления отзыва." +
                    " Невозможно обновить отзыв которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sql = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        review = getReviewById(review.getReviewId());
        addToFeedReviewUpdate(review.getReviewId());
        return review;
    }

    @Override
    public void deleteReview(Integer reviewId) {
        if (!dbContainsReview(reviewId)) {
            String message = "Ошибка запроса удаления отзыва." +
                    " Невозможно удалить отзыв которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        Integer userId = getReviewById(reviewId).getUserId();
        String sql = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sql, reviewId);
        addToFeedReviewDelete(reviewId, userId);
    }

    @Override
    public Review getReviewById(Integer reviewId) {
        if (!dbContainsReview(reviewId)) {
            String message = "Ошибка запроса отзыва." +
                    " Невозможно запросить отзыв которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.PERSON_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "WHERE R.REVIEW_ID = ? " +
                "GROUP BY R.REVIEW_ID";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeReview(rs), reviewId);
    }

    @Override
    public List<Review> getReviewsForFilm(Integer filmId, Integer count) {
        if (!dbContainsFilm(filmId)) {
            String message = "Ошибка запроса фильма." +
                    " Невозможно запросить фильм которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.PERSON_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "WHERE R.FILM_ID = ? " +
                "GROUP BY R.REVIEW_ID " +
                "ORDER BY USEFUL DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), filmId, count);
    }

    @Override
    public List<Review> getAllReviewsWithLimit(Integer count) {
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.PERSON_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "GROUP BY R.REVIEW_ID " +
                "ORDER BY USEFUL DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), count);
    }

    private boolean dbContainsReview(Integer reviewId) {
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.PERSON_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "WHERE R.REVIEW_ID = ? " +
                "GROUP BY R.REVIEW_ID";
        try {
            jdbcTemplate.queryForObject(sql, (rs, rowNum) -> makeReview(rs), reviewId);
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

    private boolean dbContainsUser(Integer userId) {
        String sqlQuery = "SELECT * FROM person WHERE person_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private Review makeReview(ResultSet rs) throws SQLException {
        int id = rs.getInt("review_id");
        String content = rs.getString("content");
        boolean isPositive = rs.getBoolean("is_positive");
        Integer userId = rs.getInt("person_id");
        Integer filmId = rs.getInt("film_id");
        Integer useful = rs.getInt("useful");
        return new Review(id, content, isPositive, userId, filmId, useful);
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

    private Set<Genre> findGenresByFilmId(Integer id) {
        String sqlQuery = "SELECT g.genre_id, g.genre_name FROM film AS f JOIN genre_films AS gf " +
                "ON f.film_id=gf.film_id JOIN genre AS g ON gf.genre_id=g.genre_id WHERE f.film_id = ?";
        return new TreeSet<>(jdbcTemplate.query(sqlQuery, this::makeGenre, id));
    }

    private Genre makeGenre(ResultSet resultSet, int rowSum) throws SQLException {
        return new Genre(resultSet.getInt("genre_id"), resultSet.getString("genre_name"));
    }

    private Director makeDirector(ResultSet resultSet, int rowSum) throws SQLException {
        return new Director(resultSet.getInt("director_id"), resultSet.getString("director_name"));
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

    private void addToFeedReviewUpdate(Integer reviewId) {
        String sqlQuery = "INSERT INTO feed (person_id, event_type, operation,entity_id,time_stamp) " +
                "VALUES (?, 'REVIEW', 'UPDATE', ?,?)";
        jdbcTemplate.update(sqlQuery, getReviewById(reviewId).getUserId(),
                reviewId, Date.from(Instant.now()));
    }

    private void addToFeedReviewCreate(Integer reviewId, Integer userId) {
        String sql = "INSERT INTO feed (person_id, event_type, operation,entity_id,time_stamp) " +
                "VALUES (?, 'REVIEW', 'ADD', ?,?)";
        jdbcTemplate.update(sql, userId, reviewId, Date.from(Instant.now()));
    }

    private void addToFeedReviewDelete(Integer reviewId, Integer userId) {
        String sqlQuery = "INSERT INTO feed (person_id, event_type, operation,entity_id,time_stamp)" +
                " VALUES (?, 'REVIEW', 'REMOVE', ?,?)";
        jdbcTemplate.update(sqlQuery, userId, reviewId, Date.from(Instant.now()));
    }
}
