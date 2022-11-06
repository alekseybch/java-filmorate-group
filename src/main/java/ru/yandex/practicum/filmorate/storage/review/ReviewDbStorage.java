package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage{

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review addReview(Review review) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("review_id");
        int reviewId = simpleJdbcInsert.executeAndReturnKey(review.toMap()).intValue();
        review.setReviewId(reviewId);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE REVIEWS SET CONTENT = ?, IS_POSITIVE = ? WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return getReviewById(review.getReviewId());
    }

    @Override
        public void deleteReview(Integer reviewId) {
        String sql = "DELETE FROM REVIEWS WHERE REVIEW_ID = ?";
        jdbcTemplate.update(sql, reviewId);
    }

    @Override
    public Review getReviewById(Integer reviewId) {
        if (!dbContainsReview(reviewId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "review with id = " + reviewId + "is not found");
        }
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.USER_ID, R.FILM_ID, " +
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
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.USER_ID, R.FILM_ID, " +
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
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.USER_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "GROUP BY R.REVIEW_ID " +
                "ORDER BY USEFUL DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs), count);
    }

    @Override
    public List<Review> getAllReviews() {
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.USER_ID, R.FILM_ID, " +
                "(SUM(CASE WHEN RL.IS_POSITIVE = TRUE THEN 1 ELSE 0 END) - " +
                "SUM(CASE WHEN RL.IS_POSITIVE = FALSE THEN 1 ELSE 0 END)) AS USEFUL " +
                "FROM REVIEWS AS R " +
                "LEFT JOIN REVIEW_LIKES AS RL ON R.REVIEW_ID = RL.REVIEW_ID " +
                "GROUP BY R.REVIEW_ID " +
                "ORDER BY USEFUL DESC ";
        return jdbcTemplate.query(sql, (rs, rowNum) -> makeReview(rs));
    }

    @Override
    public boolean dbContainsReview(Integer reviewId) {
        String sql = "SELECT R.REVIEW_ID, R.CONTENT, R.IS_POSITIVE, R.USER_ID, R.FILM_ID, " +
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

    private Review makeReview(ResultSet rs) throws SQLException {
        int id = rs.getInt("review_id");
        String content = rs.getString("content");
        boolean isPositive = rs.getBoolean("is_positive");
        Integer userId = rs.getInt("user_id");
        Integer filmId = rs.getInt("film_id");
        Integer useful = rs.getInt("useful");
        return new Review(id, content, isPositive, userId, filmId, useful);
    }
}
