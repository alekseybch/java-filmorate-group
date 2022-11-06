package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.review.likes.ReviewLikesStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.Validation;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;

    private final ReviewLikesStorage reviewLikesStorage;

    private final UserStorage userStorage;

    private final FilmStorage filmStorage;

    public Review addReview(Review review) {
        Validation.validateUserId(userStorage, review.getUserId());
        Validation.validateFilmId(filmStorage, review.getFilmId());
        Review reviewWithId = reviewStorage.addReview(review);
        return getReviewById(reviewWithId.getReviewId());
    }

    public Review updateReview(Review review) {
        Validation.validateReviewId(reviewStorage, review.getReviewId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Integer reviewId) {
        Validation.validateReviewId(reviewStorage, reviewId);
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(Integer reviewId) {
        Validation.validateReviewId(reviewStorage, reviewId);
        return reviewStorage.getReviewById(reviewId);
    }

    public List<Review> getReviewsForFilm(Integer filmId, Integer count) {
        Validation.validateCountOfLimit(count);
        if (filmId == null) {
            return reviewStorage.getAllReviewsWithLimit(count);
        } else {
            Validation.validateFilmId(filmStorage, filmId);
            return reviewStorage.getReviewsForFilm(filmId, count);
        }
    }

    public void addLike(Integer reviewId, Integer userId, boolean isPositive) {
        Validation.validateReviewId(reviewStorage, reviewId);
        Validation.validateUserId(userStorage, userId);
        reviewLikesStorage.addLike(reviewId, userId, isPositive);
    }

    public void deleteLike(Integer reviewId, Integer userId) {
        Validation.validateReviewId(reviewStorage, reviewId);
        Validation.validateUserId(userStorage, userId);
        reviewLikesStorage.deleteLike(reviewId, userId);
    }
}
