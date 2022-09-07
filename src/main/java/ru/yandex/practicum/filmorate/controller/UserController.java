package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Integer, User> users = new HashMap<>();
    private Integer id = 1;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@Valid @RequestBody User user) {
        if (users.containsValue(user)) {
            log.warn("Такой пользователь уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой пользователь уже есть");
        }
        user.setId(getId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        users.put(user.getId(), user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        if (!containsId(user)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователя с id=" + user.getId() + "нет");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое. Был использован логин");
        }
        users.put(user.getId(), user);
        log.info("Пользователь {} сохранен", user);
        return user;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        log.info("Текущее кол-во пользователей: " + users.size());
        return new ArrayList<>(users.values());
    }

    private Integer getId() {
        return id++;
    }

    private boolean containsId(User user) {
        for (User savedUser : users.values()) {
            if (savedUser.getId() != null && savedUser.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler(ResponseStatusException.class)
    private ResponseEntity<String> handleException(ResponseStatusException exception) {
        return ResponseEntity
                .status(exception.getStatus())
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<String> handleException(MethodArgumentNotValidException exception) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(HttpStatus.BAD_REQUEST + " " + exception.getFieldError().getDefaultMessage());
    }
}
