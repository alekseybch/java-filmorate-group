package ru.yandex.practicum.filmorate.storage.review.likes;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
@RequiredArgsConstructor
public class ReviewLikesDbStorage implements ReviewLikesStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(Integer reviewId, Integer userId, boolean is_positive) {
        if (!dbContainsReview(reviewId)) {
            String message = "Ошибка запроса добавления лайка." +
                    " Невозможно добавить лайк к отзыву, которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса удаления лайка." +
                    " Невозможно удалить лайк пользователяб которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sql = "INSERT INTO REVIEW_LIKES (REVIEW_ID, PERSON_ID, IS_POSITIVE) " +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, reviewId, userId, is_positive);
    }

    @Override
    public void deleteLike(Integer reviewId, Integer userId) {
        if (!dbContainsReview(reviewId)) {
            String message = "Ошибка запроса удаления лайка." +
                    " Невозможно удалить лайк к отзыву, которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса удаления лайка." +
                    " Невозможно удалить лайк пользователяб которого не существует.";
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sql = "DELETE FROM REVIEW_LIKES WHERE REVIEW_ID = ? AND PERSON_ID = ?";
        jdbcTemplate.update(sql, reviewId, userId);
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

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("person_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
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
}
