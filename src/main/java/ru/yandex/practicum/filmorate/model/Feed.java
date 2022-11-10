package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
@AllArgsConstructor
@Builder
public class Feed {
    @NotNull
    private int userId;

    private String eventType;

    private String operation;

    @NotNull
    private int eventId;

    @NotNull
    private int entityId;

    private long timestamp;
}
