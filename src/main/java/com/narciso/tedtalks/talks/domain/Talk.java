package com.narciso.tedtalks.talks.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Talk {
    private Long id;
    private String title;
    private YearMonth date;
    private long views;
    private long likes;
    private String link;
    private Long speakerId;
}
