package com.narciso.tedtalks.speakers.dao;

import com.narciso.tedtalks.speakers.domain.Speaker;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SpeakerRowMapper implements RowMapper<Speaker> {
    @Override
    public Speaker mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Speaker.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}
