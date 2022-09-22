package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private UserStorage users;
    private Integer globalId = 1;

    @Autowired
    public UserService(UserStorage users) {
        this.users = users;
    }

    public User addUser(User user) throws ResponseStatusException {
        if (users.getUsersMap().containsValue(user)) {
            log.warn("Такой пользователь уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой пользователь уже есть");
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        users.add(user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    public User updateUser(User user) throws ResponseStatusException {
        if (users.getUsersMap().get(user.getId()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с id=" + user.getId() + "нет");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        users.update(user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    public List<User> getUsers() {
        log.info("Текущее кол-во пользователей: " + users.getUsersMap().size());
        return new ArrayList<>(users.getUsersMap().values());
    }

    public String addFriend(Integer idUser, Integer friendId) throws ResponseStatusException {
        User user;
        User friend;
        if (idUser <=0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (users.getUsersMap().get(idUser) == null) {
            String message = "Ошибка добавления в друзья!" +
                    " Невозможно добавиться в друзья к пользователю с несуществующим id= " + idUser;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            user = users.getUsersMap().get(idUser);
        }
        if (users.getUsersMap().get(friendId) == null) {
            String message = "Ошибка добавления в друзья!" +
                    " Невозможно добавить в друзья несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            friend = users.getUsersMap().get(friendId);
        }
        user.addFriend(friend);
        friend.addFriend(user);
        return user.getName() + " добавил в друзья " + friend.getName();
    }

    public String deleteFriend(Integer idUser, Integer friendId) throws ResponseStatusException {
        User user;
        User friend;
        if (idUser <=0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (users.getUsersMap().get(idUser) == null) {
            String message = "Ошибка удаления из друзей!" +
                    " Невозможно удалиться из друзей несуществующего пользователя с id=" + idUser;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            user = users.getUsersMap().get(idUser);
        }
        if (users.getUsersMap().get(friendId) == null) {
            String message = "Ошибка удаления из друзей!" +
                    " Невозможно удалить из друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            friend = users.getUsersMap().get(friendId);
        }
        user.deleteFriend(friend);
        friend.deleteFriend(user);
        return user.getName() + " удалил из друзей " + friend.getName();
    }

    public List<User> getCommonFriends(Integer idUser, Integer friendId) throws ResponseStatusException {
        User user;
        User friend;
        if (idUser <=0 || friendId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id и friendId не могут быть отрицательныи либо равены 0");
        }
        if (users.getUsersMap().get(idUser) == null) {
            String message = "Ошибка запроса списка общих друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + idUser;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            user = users.getUsersMap().get(idUser);
        }
        if (users.getUsersMap().get(friendId) == null) {
            String message = "Ошибка запроса списка общих друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            friend = users.getUsersMap().get(friendId);
        }
/*        List<User> commonFriends = new ArrayList<>();
        for (Integer id : user.getCommonFriendList(friend)) {
            commonFriends.add(users.getUsersMap().get(id));
        }*/
        return user.getCommonFriendList(friend).stream().map(id -> users.getUsersMap().get(id)).collect(Collectors.toCollection(ArrayList::new));
    }

    public List<User> getFriends(Integer friendId) throws ResponseStatusException {
        User friend;
        if (friendId <=0 ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
        }
        if (users.getUsersMap().get(friendId) == null) {
            String message = "Ошибка запроса списка друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        } else {
            friend = users.getUsersMap().get(friendId);
        }
        return friend.getFriends().stream().map(id -> users.getUsersMap().get(id)).collect(Collectors.toCollection(ArrayList::new));
    }

    public User getUser(Integer userId) {
        if (userId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id не может быть отрицательным либо равен 0");
        }
        if (users.getUsersMap().get(userId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с id= " + userId + " не существует");
        }
        return users.getUsersMap().get(userId);
    }

    private Integer getNextId() {
        return globalId++;
    }
}
