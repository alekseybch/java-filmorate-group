package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    private final UserStorage users;
    private final FilmStorage films;

    public User addUser(User user) throws ResponseStatusException {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        return users.add(user);
    }

    public User updateUser(User user) throws ResponseStatusException {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        users.update(user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    public List<User> getUsers() {
        log.info("Текущее кол-во пользователей: " + users.getUsersList().size());
        return users.getUsersList();
    }

    public List<Feed> getUserFeed(Integer userId) {
        log.info("Новостная лента пользователя: {} {}", users.getUser(userId).getName(), users.getUserFeed(userId));
        return users.getUserFeed(userId);
    }

    public void addFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <= 0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        users.addFriend(userId, friendId);
        log.info("Пользователь с id=" + userId + " добавил в друзья пользователя с id= " + friendId);
    }

    public void deleteFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <= 0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невозможно удалить из друзей самого себя");
        }
        users.deleteFriend(userId, friendId);
        log.info("Пользователь с id=" + userId + " удалил пользователя с id=" + friendId);
    }

    public List<User> getCommonFriends(Integer userId, Integer friendId) throws ResponseStatusException {
        if (userId <= 0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (userId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невозможно запросить общих друзей самого себя");
        }
        return users.getCommonFriends(userId, friendId);
    }

    public List<User> getFriends(Integer friendId) throws ResponseStatusException {

        if (friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }

        return users.getFriends(friendId);
    }

    public User getUser(Integer userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        return users.getUser(userId);
    }

    public void delete(Integer userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        users.delete(userId);
        log.info("Пользователь с id=" + userId + " удален");
    }

    public List<Film> getRecommendations(Integer userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        List<Film> allLikedFilms = films.getLikedFilms();
        List<Film> userLikes;
        Map<Integer, List<Film>> usersAndLikes = new HashMap<>();
        for (Film film : allLikedFilms) {
            for (User user : film.getLikes()) {
                if (!usersAndLikes.containsKey(user.getId())) {
                    List<Film> likedFilms = new ArrayList<>();
                    likedFilms.add(film);
                    usersAndLikes.put(user.getId(), likedFilms);
                } else {
                    usersAndLikes.get(user.getId()).add(film);
                }
            }
        }

        userLikes = usersAndLikes.get(userId);
        if (userLikes == null) {
            log.info("У пользователя с id={} нет лайков", userId);
            return new ArrayList<>();
        }
        Map<Integer, Integer> frequencyLikes = new HashMap<>(); // userId/freq
        for (Map.Entry<Integer, List<Film>> entry: usersAndLikes.entrySet()) {
            if (entry.getKey().equals(userId)) {
                continue;
            }
            if (!frequencyLikes.containsKey(entry.getKey())) {
                frequencyLikes.put(entry.getKey(), 0);
            }
            Integer freq = frequencyLikes.get(entry.getKey());
            userLikes.stream().filter(film -> entry.getValue().contains(film))
                    .forEach(film -> frequencyLikes.put(entry.getKey(), freq + 1));
        }

        int maxFreq = 0;
        Integer id = null;
        for ( Map.Entry<Integer, Integer> entry : frequencyLikes.entrySet()) {
            if (maxFreq < entry.getValue()) {
                maxFreq = entry.getValue();
                id = entry.getKey();
            }
        }
        if (maxFreq == 0) {
            log.info("У пользователя с id={} нет общих лайков с кем либо", userId);
            return new ArrayList<>();
        }
        return usersAndLikes.get(id).stream().filter(film -> !userLikes.contains(film)).collect(Collectors.toList());
    }

}
