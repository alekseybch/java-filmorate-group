package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Date;


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

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date timestamp;

}
