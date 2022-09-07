package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private static final LocalDate brithDay = LocalDate.of(1993, 10, 27);

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addAndGetUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        List<User> expectFilms = new ArrayList<>(List.of(user1));

        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //when
        mockMvc.perform(get("/users")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectFilms, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<User>>() {
                                }), "Пользователи не совпадают")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addDuplicateUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void addIncorrectEmailUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("this-incorrect?.email@").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void addIncorrectLoginUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void useLoginInsteadName() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.name").value("user_login")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void addIncorrectBrithDay() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay.plusYears(1000)).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
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
    public void addAndUpdateUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())

                //then
                .andExpectAll(
                        status().isCreated(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //given
        user1.setEmail("simple2@email.ru");
        user1.setLogin("user_login2");
        List<User> expectUsers = new ArrayList<>(List.of(user1));
        //when
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(user1, objectMapper.readValue(result.getResponse().getContentAsString()
                                , User.class), "Пользователи не совпадают")
                );
        //when
        mockMvc.perform(get("/users")).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        result -> assertEquals(expectUsers, objectMapper.readValue(result.getResponse().getContentAsString()
                                , new TypeReference<ArrayList<User>>() {
                                }), "Пользователи не совпадают")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateUnknownUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();

        //when
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void updateIncorrectEmailUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setEmail("this-incorrect?.email@");
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void updateIncorrectLoginUser() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setLogin("");
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
    public void updateUseLoginInsteadName() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("name").birthday(brithDay)
                .build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setName("");
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
                        .contentType(MediaType.APPLICATION_JSON)).andDo(print())
                //then
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.name").value("user_login")
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    public void updateIncorrectBrithDay() throws Exception {
        //given
        User user1 = User.builder().id(1).email("simple@email.ru").login("user_login").name("user name")
                .birthday(brithDay).build();
        //when
        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)).andDo(print());
        user1.setBirthday(brithDay.plusYears(1000));
        mockMvc.perform(put("/users").content(objectMapper.writeValueAsString(user1))
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
