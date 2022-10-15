package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryUserStorage {
    private final Map<Integer, User> users = new HashMap<>();

    //@Override
    public void add(User user) {
        users.put(user.getId(), user);
    }

   // @Override
    public void delete(User user) {
        users.remove(user.getId());
    }

   // @Override
    public void update(User user) {
        users.put(user.getId(), user);
    }

    //@Override
    public Map<Integer, User> getUsersList() {
        return users;
    }
}
