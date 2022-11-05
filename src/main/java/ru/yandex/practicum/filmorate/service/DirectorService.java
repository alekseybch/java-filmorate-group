package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage directors;

    @Autowired
    public DirectorService(DirectorStorage directors) {
        this.directors = directors;
    }

    public List<Director> getDirectorList() {
        log.info("Текущее кол-во режиссеров: " + directors.getDirectorsList().size());
        return directors.getDirectorsList();
    }

    public Director getDirector(Integer directorId) {
        if (directorId <= 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "id не может быть отрицательным либо равен 0");
        }
        return  directors.getDirector(directorId);
    }

    public Director addDirector(Director director) {
        return directors.add(director);
    }

    public Director updateDirector(Director director) {
        directors.update(director);
        log.info("Режиссер {} сохранен", director);
        return director;
    }

    public void delete(Integer directorId) {
            if (directorId <= 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "id не может быть отрицательным либо равен 0");
            }
            directors.delete(directorId);
            log.info("Режиссер с id=" + directorId + " удален");
    }
}
