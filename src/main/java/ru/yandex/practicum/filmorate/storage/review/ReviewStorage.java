package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Integer reviewId);

    Review getReviewById(Integer reviewId);

    List<Review> getReviewsForFilm(Integer filmId, Integer count);

    List<Review> getAllReviewsWithLimit(Integer count);
}
