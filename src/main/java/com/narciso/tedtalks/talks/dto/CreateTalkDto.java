package com.narciso.tedtalks.talks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CreateTalkDto {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Date cannot be empty (format: yyyy-MM)")
    private String date;

    @NotNull(message = "Views cannot be null")
    @PositiveOrZero(message = "Views must be zero or positive")
    private int views;

    @NotNull(message = "Likes cannot be null")
    @PositiveOrZero(message = "Likes must be zero or positive")
    private int likes;

    private String link;
    private Long speakerId;
    private String speakerName;
}