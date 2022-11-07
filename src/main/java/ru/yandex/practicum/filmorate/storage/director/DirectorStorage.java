package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {
    Director add(Director director);

    void delete(Integer directorId);

    void update(Director director);

    List<Director> getDirectorsList();

    Director getDirector(Integer directorId);
}
