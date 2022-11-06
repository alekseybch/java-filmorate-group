package ru.yandex.practicum.filmorate.validation;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

@Slf4j
@Component
@AllArgsConstructor
public class Validation {

    public static void validateReviewId(ReviewStorage reviewStorage, Integer reviewId) {
        if (reviewId <= 0) {
            log.warn("user entered invalid count = " + reviewId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid value of reviewId = " + reviewId);
        }
        if (!reviewStorage.dbContainsReview(reviewId)) {
            log.warn("user entered invalid id of review = " + reviewId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "review with id = " + reviewId + " not found");
        }
    }

    public static void validateCountOfLimit(Integer count) {
        if (count <= 0) {
            log.warn("user entered invalid count = " + count);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid value of count = " + count);
        }
    }

    public static void validateFilmId(FilmStorage filmStorage, Integer filmId) {
        if (filmId <= 0) {
            log.warn("user entered invalid id of film = " + filmId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "film with id = " + filmId + " not found");
        }
        if (!filmStorage.dbContainsFilm(filmId)) {
            log.warn("user entered invalid id of film = " + filmId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "film with id = " + filmId + " not found");
        }
    }

    public static void validateUserId(UserStorage userStorage, Integer userId) {
        if (userId <= 0) {
            log.warn("user entered invalid id of user = " + userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "user with id = " + userId + " not found");
        }
        if (!userStorage.dbContainsUser(userId)) {
            log.warn("user entered invalid id of user = " + userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user with id = " + userId + " not found");
        }
    }
}
