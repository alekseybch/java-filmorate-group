package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.RandomString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate date = LocalDate.of(1895, 12, 29);

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addAndGetFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60L)
                .build();
        List<Film> expectFilms = new ArrayList<>(List.of(film1));

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        //when
        mockMvc.perform(get("/films")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>(){}), "Фильмы не совпадают")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addDuplicateFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60L)
                .build();

        //when
        //filmController.addFilm(film1);
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addIncorrectDateFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description")
                .releaseDate(date.minusDays(2)).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addIncorrectNameFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("").description("description")
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addIncorrectDescriptionFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description(RandomString.make(201))
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addIncorrectDurationFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description")
                .releaseDate(date).duration(0L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addAndUpdateFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60L)
                .build();
        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        //given
        film1.setName("update film1");
        film1.setDescription("update description");
        film1.setReleaseDate(date.plusYears(1));
        film1.setDuration(70L);
        List<Film> expectFilms = new ArrayList<>(List.of(film1));
        //when
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(film1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Film.class), "Фильмы не совпадают")
                );
        mockMvc.perform(get("/films")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Film>>(){}), "Фильмы не совпадают")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateUnknownFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description").releaseDate(date).duration(60L)
                .build();

        //when
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isNotFound(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateIncorrectDateFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description")
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON));
        film1.setReleaseDate(date.minusDays(2));
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateIncorrectNameFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film").description("description")
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        film1.setName("");
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateIncorrectDescriptionFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description")
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        film1.setDescription(RandomString.make(201));
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateIncorrectDurationFilm() throws Exception {
        //given
        Film film1 = Film.builder().id(1).name("film1").description("description")
                .releaseDate(date).duration(60L).build();

        //when
        mockMvc.perform(post("/films").content(objectMapper.writeValueAsString(film1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        film1.setDuration(-2L);
        mockMvc.perform(put("/films").content(objectMapper.writeValueAsString(film1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isBadRequest(),
                        result -> {
                            assertNotNull(result.getResponse().getContentAsString()
                                    , "Отсутствует тело сообщения");
                            assertFalse(result.getResponse().getContentAsString().isBlank()
                                    , "Тело ответа с сообщением пустое");
                        }
                );
    }
}
