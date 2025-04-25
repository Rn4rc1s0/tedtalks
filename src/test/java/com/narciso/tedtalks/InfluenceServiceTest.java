package com.narciso.tedtalks.speakers.service;

import com.narciso.tedtalks.speakers.domain.MostInfluentialSpeaker;
import com.narciso.tedtalks.speakers.dto.SpeakerInfluenceDto;
import com.narciso.tedtalks.speakers.strategy.InfluenceStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InfluenceServiceTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private InfluenceStrategy strategy;

    @InjectMocks
    private InfluenceService influenceService;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<SqlParameterSource> paramsCaptor;

    @Captor
    private ArgumentCaptor<RowMapper<SpeakerInfluenceDto>> influenceDtoRowMapperCaptor;

    @Captor
    private ArgumentCaptor<RowMapper<MostInfluentialSpeaker>> mostInfluentialSpeakerRowMapperCaptor;

    @BeforeEach
    void setUp() {
        SpeakerInfluenceDto speaker1Dto = new SpeakerInfluenceDto(1L, "Speaker One", 1000L, 100L, BigDecimal.valueOf(110.0));
        SpeakerInfluenceDto speaker2Dto = new SpeakerInfluenceDto(2L, "Speaker Two", 2000L, 50L, BigDecimal.valueOf(205.0)); // Higher score

        MostInfluentialSpeaker speaker1MostInfluential = new MostInfluentialSpeaker(1L, "Speaker One", BigDecimal.valueOf(110.0));
        MostInfluentialSpeaker speaker2MostInfluential = new MostInfluentialSpeaker(2L, "Speaker Two", BigDecimal.valueOf(205.0));
    }

    private <T> Answer<List<T>> simulateQueryWithMapper(List<Object[]> rowData, RowMapper<T> mapper) {
        return invocation -> {
            // RowMapper<T> mapper = invocation.getArgument(2); // Get the actual mapper passed to query
            List<T> results = new java.util.ArrayList<>();
            int rowNum = 0;
            for (Object[] data : rowData) {
                ResultSet rs = mock(ResultSet.class);
                // Simulate ResultSet data based on expected columns
                when(rs.getLong("speaker_id")).thenReturn((Long) data[0]); // Adjust column names/types as needed
                when(rs.getString("name")).thenReturn((String) data[1]);
                when(rs.getLong("total_views")).thenReturn((Long) data[2]);
                when(rs.getLong("total_likes")).thenReturn((Long) data[3]);
                // Add stubs for 'id' if testing MostInfluentialSpeaker mapper
                when(rs.getLong("id")).thenReturn((Long) data[0]); // Assuming id is same as speaker_id here

                // Execute the actual mapper's logic - THIS WILL CALL strategy.score()
                results.add(mapper.mapRow(rs, rowNum++));
            }
            return results;
        };
    }

    @Test
    @DisplayName("analyzeInfluence should return empty list when no data found")
    void analyzeInfluence_NoData_ShouldReturnEmptyList() {
        Optional<Integer> yearOptional = Optional.of(2023);

        when(jdbcTemplate.query(sqlCaptor.capture(), paramsCaptor.capture(), influenceDtoRowMapperCaptor.capture()))
                .thenReturn(Collections.emptyList());

        List<SpeakerInfluenceDto> result = influenceService.analyzeInfluence(yearOptional);

        assertThat(result).isEmpty();

        verify(jdbcTemplate).query(anyString(), any(SqlParameterSource.class), any(RowMapper.class));
        verifyNoInteractions(strategy);
        verifyNoMoreInteractions(jdbcTemplate);
    }

    @Test
    @DisplayName("findMostInfluentialSpeaker should return empty optional when no data found")
    void findMostInfluentialSpeaker_NoDataFound_ShouldReturnEmptyOptional() {
        int year = 2023;

        when(jdbcTemplate.query(sqlCaptor.capture(), paramsCaptor.capture(), mostInfluentialSpeakerRowMapperCaptor.capture()))
                .thenReturn(Collections.emptyList());

        Optional<MostInfluentialSpeaker> result = influenceService.findMostInfluentialSpeaker(year);

        assertThat(result).isNotPresent();

        verify(jdbcTemplate).query(anyString(), any(SqlParameterSource.class), any(RowMapper.class));
        verifyNoInteractions(strategy);
        verifyNoMoreInteractions(jdbcTemplate);
    }

}