package com.narciso.tedtalks.speakers.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MostInfluentialSpeaker {
    private Long id;
    private String name;
    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="0.00")
    private BigDecimal score;
}
