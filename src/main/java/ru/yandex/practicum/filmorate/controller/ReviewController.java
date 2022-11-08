package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        Review addedReview = reviewService.addReview(review);
        log.info("Отзыв с id = {} добавлен", addedReview.getReviewId());
        return addedReview;
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        Review updatedReview = reviewService.updateReview(review);
        log.info("Отзыв с id = {} обновлен", updatedReview.getReviewId());
        return updatedReview;
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable("id") Integer reviewId) {
        reviewService.deleteReview(reviewId);
        log.info("Отзыв с id = {} удален", reviewId);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable("id") Integer reviewId) {
        Review review = reviewService.getReviewById(reviewId);
        log.info("Отзыв с id = {} найден", reviewId);
        return review;
    }

    @GetMapping
    public List<Review> getReviewsForFilm(@RequestParam(required = false) Integer filmId,
                                          @RequestParam(defaultValue = "10") Integer count) {
        List<Review> reviews = reviewService.getReviewsForFilm(filmId, count);
        if (!reviews.isEmpty()) {
            log.info("Количество отзывов {}, id самого полезного отзыва {}", reviews.size(), reviews.get(0).getReviewId());
        }
        return reviews;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable("id") Integer reviewId, @PathVariable Integer userId) {
        reviewService.addLike(reviewId, userId, true);
        log.info("добавлен лайк");
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") Integer reviewId, @PathVariable Integer userId) {
        reviewService.deleteLike(reviewId, userId);
        log.info("удален лайк");
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable("id") Integer reviewId, @PathVariable Integer userId) {
        reviewService.addLike(reviewId, userId, false);
        log.info("добавлен дизлайк");
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable("id") Integer reviewId, @PathVariable Integer userId) {
        reviewService.deleteLike(reviewId, userId);
        log.info("удален дизлайк");
    }
}