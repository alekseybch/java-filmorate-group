package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User add(User user);

    void delete(Integer userId);

    void update(User user);

    List<User> getUsersList();

    void addFriend(Integer userId, Integer friendId);

    void deleteFriend(Integer userId, Integer friendId);

    List<User> getCommonFriends(Integer userId, Integer friendId);

    List<User> getFriends(Integer friendId);

    User getUser(Integer userId);

    List<Film> getRecommendations(int userId);
}
