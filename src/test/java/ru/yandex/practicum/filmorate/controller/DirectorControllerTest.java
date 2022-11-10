package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Sql(scripts = {"file:src/main/resources/schema.sql"})
public class DirectorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void addAndGetDirector() throws Exception {
        //given
        Director director1 = Director.builder().name("name")
                .build();
        director1.setId(1);
        List<Director> expectFilms = new ArrayList<>(List.of(director1));

        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(director1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Director.class), "Режиссеры не совпадают")
                );
        //when
        mockMvc.perform(get("/directors")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Director>>() {
                                }), "Режиссеры не совпадают")
                );
    }

    @Test
    public void addDuplicateDirector() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name("name")
                .build();
        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
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
    public void addIncorrectName() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name(null)
                .build();
        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
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
    public void addAndUpdateDirector() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name("name")
                .build();
        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())

                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(director1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Director.class), "Режиссеры не совпадают")
                );
        //given
        director1.setName("newName");
        director1.setId(1);
        List<Director> expectDirectors = new ArrayList<>(List.of(director1));
        //when
        mockMvc.perform(put("/directors").content(objectMapper.writeValueAsString(director1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(director1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Director.class), "Режиссеры не совпадают")
                );
        //when
        mockMvc.perform(get("/directors")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectDirectors, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<Director>>() {
                                }), "Режиссеры не совпадают")
                );
    }

    @Test
    public void updateUnknownDirector() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name("name")
                .build();

        //when
        mockMvc.perform(put("/directors").content(objectMapper.writeValueAsString(director1))
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
    public void updateIncorrectName() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name("name")
                .build();
        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        director1.setName(null);
        mockMvc.perform(put("/directors").content(objectMapper.writeValueAsString(director1))
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
    public void addAndDeleteDirector() throws Exception {
        //given
        Director director1 = Director.builder().id(1).name("name")
                .build();

        //when
        mockMvc.perform(post("/directors").content(objectMapper.writeValueAsString(director1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(director1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , Director.class), "Режиссеры не совпадают")
                );
        //when
        mockMvc.perform(delete("/directors/1"))
                //then
                .andExpectAll(
                        status().isOk()
                );
    }

    @Test
    public void deleteUnknownDirector() throws Exception {
        //when
        mockMvc.perform(delete("/directors/-1"))
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
}
