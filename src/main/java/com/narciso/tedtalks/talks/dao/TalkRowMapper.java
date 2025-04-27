package com.narciso.tedtalks.talks.dao;

import com.narciso.tedtalks.talks.dto.TalkDto;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class TalkRowMapper implements RowMapper<TalkDto> {
    private static final DateTimeFormatter DB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public TalkDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        long speakerIdRaw = rs.getLong("speaker_id");
        Long speakerId = rs.wasNull() ? null : speakerIdRaw;

        return TalkDto.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .date(YearMonth.parse(rs.getString("date"), DB_DATE_FORMATTER))
                .views(rs.getInt("views"))
                .likes(rs.getInt("likes"))
                .link(rs.getString("link"))
                .speakerId(speakerId)
                .speakerName(rs.getString("speaker_name"))
                .build();
    }
}
