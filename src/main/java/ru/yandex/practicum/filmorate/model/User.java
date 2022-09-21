package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class User {
    @EqualsAndHashCode.Exclude
    private Integer id;
    @Email(message = "некорректный email")
    private String email;
    @NotBlank(message = "Логин не может быть пустым или null")
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Integer> friends;

    public User(Integer id, String email, String login, String name, LocalDate birthday, Set<Integer> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        if (friends == null) {
            this.friends = new HashSet<>();
        } else {
            this.friends = friends;
        }
    }

    public void addFriend(User user) throws ResponseStatusException {
        if (friends.contains(user.getId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR
                    , "Невозможно добавить в друзья пользователя который уже в друзьях");
        }
        friends.add(user.getId());
    }

    public void deleteFriend(User user) throws ResponseStatusException {
        if (!friends.contains(user.getId())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR
                    , "Невозможно удалить пользователя которого нет в друзьях");
        }
        friends.remove(user.getId());
    }

    public List<Integer> getCommonFriendList(User friend) {
        return friend.getFriends().stream().filter(friends::contains).collect(Collectors.toCollection(ArrayList::new));
    }

}
