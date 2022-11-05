package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@Slf4j
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Director add(Director director) {
        if (dbContainsDirector(director)) {
            log.warn("Такой режиссер уже есть");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Такой режиссер уже есть");
        }
        Integer directorId = addDirectorInfo(director);
        director.setId(directorId);
        log.info("Режиссер {} сохранен", director);
        return getDirector(directorId);
    }

    @Override
    public void delete(Integer directorId) {
        String sqlQuery = "DELETE FROM director WHERE director_id = ?";
        if (jdbcTemplate.update(sqlQuery, directorId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Режиссера с id=" + directorId + " нет");
        }
    }

    @Override
    public void update(Director director) {
        String sqlQuery = "UPDATE director " +
                "SET director_name = ? WHERE director_id = ?";
        if (jdbcTemplate.update(sqlQuery, director.getName(), director.getId()) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Режиссера с id=" + director.getId() + " нет");
        }
    }

    @Override
    public List<Director> getDirectorsList() {
        String sqlQuery = "SELECT * FROM director";
        return jdbcTemplate.query(sqlQuery, this::makeDirector);
    }

    @Override
    public Director getDirector(Integer directorId) {
        if (!dbContainsDirector(directorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Режиссера с id= " + directorId + " не существует");
        }
        String sqlQuery = "SELECT * FROM director WHERE director_id = ?";
        return jdbcTemplate.queryForObject(sqlQuery, this::makeDirector, directorId);
    }

    private int addDirectorInfo(Director director) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("director")
                .usingGeneratedKeyColumns("director_id");
        return simpleJdbcInsert.executeAndReturnKey(director.toMap()).intValue();
    }

    private Director makeDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("director_name"))
                .build();
    }

    private boolean dbContainsDirector(Director director) {
        String sqlQuery = "SELECT * FROM director WHERE director_name = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeDirector, director.getName());
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean dbContainsDirector(Integer directorId) {
        String sqlQuery = "SELECT * FROM director WHERE director_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, this::makeDirector, directorId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }
}
