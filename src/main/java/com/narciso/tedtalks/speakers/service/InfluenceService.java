package com.narciso.tedtalks.speakers.service;

import com.narciso.tedtalks.speakers.domain.MostInfluentialSpeaker;
import com.narciso.tedtalks.speakers.dto.SpeakerInfluenceDto;
import com.narciso.tedtalks.speakers.strategy.InfluenceStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InfluenceService {
    private final NamedParameterJdbcTemplate jdbc;
    private final InfluenceStrategy strategy;

    public InfluenceService(NamedParameterJdbcTemplate jdbc,
//                            @Qualifier("compositeStrategy")
                            InfluenceStrategy strategy) {

        this.jdbc = jdbc;
        this.strategy = strategy;
    }

    public List<SpeakerInfluenceDto> analyzeInfluence(Optional<Integer> year) {
        StringBuilder sql = new StringBuilder("""
            SELECT s.id          AS speaker_id,
                   s.name        AS name,
                   SUM(t.views)  AS total_views,
                   SUM(t.likes)  AS total_likes
              FROM speakers s
              JOIN talks t ON t.speaker_id = s.id
            """);
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (year.isPresent()) {
            sql.append(" WHERE SUBSTRING(t.date,1,4) = :year");
            params.addValue("year", String.valueOf(year.get()));
        }
        sql.append(" GROUP BY s.id, s.name");

        List<SpeakerInfluenceDto> list = jdbc.query(
                sql.toString(),
                params,
                (rs, rowNum) -> {
                    long views = rs.getLong("total_views");
                    long likes = rs.getLong("total_likes");
                    BigDecimal score = strategy.score(views, likes);
                    return new SpeakerInfluenceDto(
                            rs.getLong("speaker_id"),
                            rs.getString("name"),
                            views,
                            likes,
                            score
                    );
                }
        );

        return list.stream()
                .sorted(Comparator.comparing(SpeakerInfluenceDto::getScore).reversed())
                .collect(Collectors.toList());
    }

    public Optional<MostInfluentialSpeaker> findMostInfluentialSpeaker(int year) {
        String sql = """
            SELECT s.id, s.name, SUM(t.views) AS total_views, SUM(t.likes) AS total_likes
            FROM speakers s
            JOIN talks t ON t.speaker_id = s.id
            WHERE SUBSTRING(t.date,1,4) = :year
            GROUP BY s.id, s.name
        """;
        var params = new MapSqlParameterSource().addValue("year", String.valueOf(year));

        return jdbc.query(sql, params, (rs, rowNum) -> {
                    long views = rs.getLong("total_views");
                    long likes = rs.getLong("total_likes");
                    BigDecimal score = strategy.score(views, likes);
                    return new MostInfluentialSpeaker(
                            rs.getLong("id"),
                            rs.getString("name"),
                            score
                    );
                }).stream()
                .max((a, b) -> a.getScore().compareTo(b.getScore()));
    }
}
