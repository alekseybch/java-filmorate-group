package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("UserDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(User user) {
        if (dbContainsUser(user)) {
            log.warn("Такой пользователь уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой пользователь уже есть");
        }
        Integer userId = addUserInfo(user);
        user.setId(userId);
        String sqlQuery = "INSERT INTO friend_request (sender_id, addressee_id) VALUES (?, ?)";
        user.getFriends().stream().map(friend -> jdbcTemplate.update(sqlQuery, userId, friend));
    }

    public void delete(User user) {
    }
    @Override
    public void update(User user) {
        String sqlQuery = "UPDATE person " +
                "SET email = ?, login = ?, name = ?, birthday = ? WHERE person_id = ?";
        if (jdbcTemplate.update(sqlQuery, user.getEmail(), user.getLogin(), user.getName()
                , user.getBirthday(), user.getId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с id=" + user.getId() + " нет");
        }
    }

    @Override
    public List<User> getUsersList() {
        String sqlQuery = "SELECT * FROM person";
        return jdbcTemplate.query(sqlQuery, this::makeUser);
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка добавления в друзья!" +
                    " Невозможно добавиться в друзья к пользователю с несуществующим id= " + userId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(friendId)) {
            String message = "Ошибка добавления в друзья!" +
                    " Невозможно добавить в друзья несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "INSERT INTO friend_request (sender_id, addressee_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sqlQuery, userId, friendId);
        } catch (DuplicateKeyException e) {
            String message = "Ошибка запроса добавления в друзья." +
                    " Невозможно добавить в друзья пользователя который уже в друзьях";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        } catch (DataIntegrityViolationException e) {
            String message = "Ошибка запроса добавления в друзья." +
                    " Невозможно добавиться в друзья самому к себе";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Override
    public void deleteFriend(Integer userId, Integer friendId) throws ResponseStatusException {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка удаления из друзей!" +
                    " Невозможно удалиться из друзей несуществующего пользователя с id=" + userId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(friendId)) {
            String message = "Ошибка удаления из друзей!" +
                    " Невозможно удалить из друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "DELETE FROM friend_request WHERE sender_id = ? AND addressee_id = ?";
        if (jdbcTemplate.update(sqlQuery, userId, friendId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Лайка от пользователя с id=" + userId + " у фильма с id=" + friendId + " нет");
        }
    }

    @Override
    public List<User> getCommonFriends(Integer userId, Integer friendId) throws ResponseStatusException {
        if (!dbContainsUser(userId)) {
            String message = "Ошибка запроса списка общих друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + userId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        if (!dbContainsUser(friendId)) {
            String message = "Ошибка запроса списка общих друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "SELECT * " +
                "FROM person " +
                "WHERE person_id IN " +
                "(SELECT * FROM (SELECT  addressee_id " +
                "FROM FRIEND_REQUEST " +
                "WHERE sender_id = ? OR sender_id = ? )  GROUP BY addressee_id HAVING COUNT(addressee_id) > 1)";
        return jdbcTemplate.query(sqlQuery, this::makeFriendUser, userId, friendId);
    }

    @Override
    public List<User> getFriends(Integer friendId) {
        if (!dbContainsUser(friendId)) {
            String message = "Ошибка запроса списка друзей!" +
                    " Невозможно получить список друзей несуществующего пользователя с id=" + friendId;
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
        }
        String sqlQuery = "SELECT * FROM person " +
                "WHERE person_id IN (SELECT addressee_id FROM friend_request WHERE sender_id = ?)";
        return jdbcTemplate.query(sqlQuery, this::makeFriendUser, friendId);
    }

    @Override
    public User getUser(Integer userId) throws ResponseStatusException {
        if (!dbContainsUser(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с id= " + userId + " не существует");
        }
        String sqlQuery = "SELECT * FROM person WHERE person_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
    }

    private int addUserInfo(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("person")
                .usingGeneratedKeyColumns("person_id");
        return simpleJdbcInsert.executeAndReturnKey(user.toMap()).intValue();
    }

    private User makeUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = User.builder()
                .id(resultSet.getInt("person_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
        String sqlQuery = "SELECT * FROM person WHERE person_id IN (SELECT addressee_id  FROM friend_request WHERE sender_id = ?)";
        user.getFriends().addAll(jdbcTemplate.query(sqlQuery, this::makeFriendUser, user.getId()));
        return user;
    }

    private User makeFriendUser(ResultSet resultSet, int rowNum) throws SQLException {
        return User.builder()
                .id(resultSet.getInt("person_id"))
                .email(resultSet.getString("email"))
                .login(resultSet.getString("login"))
                .name(resultSet.getString("name"))
                .birthday(resultSet.getDate("birthday").toLocalDate())
                .build();
    }

    private boolean dbContainsUser(User user) {
        String sqlQuery = "SELECT * FROM person WHERE email = ? AND login = ? AND name = ? AND birthday = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeUser, user.getEmail(), user.getLogin(),
                    user.getName(), user.getBirthday());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsUser(Integer userId) {
        String sqlQuery = "SELECT * FROM person WHERE person_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeUser, userId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
