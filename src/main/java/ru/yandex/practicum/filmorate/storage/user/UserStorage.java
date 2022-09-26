package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    void add(User user);

    void delete(User user);

    void update(User user);

    Map<Integer, User> getUsersMap();
}
