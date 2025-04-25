package com.narciso.tedtalks.speakers.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SpeakerInfluenceDto {
    private Long speakerId;
    private String name;
    private long totalViews;
    private long totalLikes;
    @JsonFormat(shape= JsonFormat.Shape.NUMBER_FLOAT, pattern="0.00")
    private BigDecimal score;
}
