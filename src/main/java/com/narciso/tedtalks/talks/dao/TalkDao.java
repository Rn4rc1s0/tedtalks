package com.narciso.tedtalks.talks.dao;

import com.narciso.tedtalks.talks.domain.Talk;
import com.narciso.tedtalks.talks.dto.TalkDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder; 
import org.springframework.jdbc.support.KeyHolder; 
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map; 
import java.util.Objects; 
import java.util.Optional;

@Repository
public class TalkDao {
    private final NamedParameterJdbcTemplate jdbc;
    private final TalkRowMapper talkRowMapper = new TalkRowMapper();
    private static final DateTimeFormatter DB_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    public TalkDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Talk create(Talk t) {
        var sql = "INSERT INTO talks (title, date, views, likes, link, speaker_id) " +
                "VALUES (:title, :dateStr, :views, :likes, :link, :speakerId)";
        var params = new MapSqlParameterSource()
                .addValue("title", t.getTitle())
                .addValue("dateStr", t.getDate().format(DB_DATE_FORMATTER))
                .addValue("views", t.getViews())
                .addValue("likes", t.getLikes())
                .addValue("link", t.getLink())
                .addValue("speakerId", t.getSpeakerId());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(sql, params, keyHolder, new String[]{"id"});

        Long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        t.setId(generatedId);
        return t;
    }

    public void update(Talk talk) {
        var sql = "UPDATE talks SET " +
                "title = :title, " +
                "date = :dateStr, " +
                "views = :views, " +
                "likes = :likes, " +
                "link = :link, " +
                "speaker_id = :speakerId " +
                "WHERE id = :id";

        var params = new MapSqlParameterSource()
                .addValue("id", talk.getId())
                .addValue("title", talk.getTitle())
                .addValue("dateStr", talk.getDate().format(DB_DATE_FORMATTER))
                .addValue("views", talk.getViews())
                .addValue("likes", talk.getLikes())
                .addValue("link", talk.getLink())
                .addValue("speakerId", talk.getSpeakerId());

        jdbc.update(sql, params);
    }

    public void delete(Long id) {
        var sql = "DELETE FROM talks WHERE id = :id";
        jdbc.update(sql, Map.of("id", id));
    }


    public Optional<TalkDto> findById(Long id) {
        String sql = """
            SELECT t.*, s.name AS speaker_name
            FROM talks t
            JOIN speakers s ON t.speaker_id = s.id
            WHERE t.id = :id
            """;
        var params = new MapSqlParameterSource().addValue("id", id);
        try {
            TalkDto talkDto = jdbc.queryForObject(sql, params, talkRowMapper);
            return Optional.ofNullable(talkDto);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Page<TalkDto> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM talks";
        Long total = jdbc.queryForObject(countSql, new MapSqlParameterSource(), Long.class);
        long totalElements = (total != null) ? total : 0L;

        String sortClause = buildSortClause(pageable.getSort());
        String dataSql = """
            SELECT t.*, s.name AS speaker_name
            FROM talks t
            JOIN speakers s ON t.speaker_id = s.id
            """ + sortClause + " LIMIT :limit OFFSET :offset";

        var params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<TalkDto> content = jdbc.query(dataSql, params, talkRowMapper);

        return new PageImpl<>(content, pageable, totalElements);
    }

    public Optional<Talk> findByTitleAndSpeakerAndDate(String title, long speakerId, YearMonth date) {
        var sql = "SELECT * FROM talks WHERE title = :title AND speaker_id = :sid AND date = :dateStr";
        var params = new MapSqlParameterSource()
                .addValue("title", title)
                .addValue("sid", speakerId)
                .addValue("dateStr", date.format(DB_DATE_FORMATTER));
        try {
            Talk talk = jdbc.queryForObject(sql, params, (rs, rowNum) ->
                    Talk.builder()
                            .id(rs.getLong("id"))
                            .title(rs.getString("title"))
                            .date(YearMonth.parse(rs.getString("date"), DB_DATE_FORMATTER))
                            .views(rs.getInt ("views"))
                            .likes(rs.getInt("likes"))
                            .link(rs.getString("link"))
                            .speakerId(rs.getLong("speaker_id"))
                            .build()
            );
            return Optional.ofNullable(talk);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private String buildSortClause(Sort sort) {
        StringBuilder sortClause = new StringBuilder("ORDER BY ");
        if (sort.isSorted()) {
            boolean first = true;
            for (Sort.Order order : sort) {
                String property = order.getProperty();
                if (!isValidSortProperty(property)) {
                    continue;
                }
                if (!first) {
                    sortClause.append(", ");
                }
                String columnName = mapPropertyToColumn(property);
                sortClause.append(columnName).append(" ").append(order.getDirection().name());
                first = false;
            }
            if (first) {
                sortClause.append("title ASC");
            }
        } else {
            sortClause.append("title ASC");
        }
        return sortClause.toString();
    }

    private boolean isValidSortProperty(String property) {
        return property != null && List.of("id", "title", "date", "views", "likes", "link", "speakerId").contains(property);
    }

    private String mapPropertyToColumn(String property) {
        if ("speakerId".equalsIgnoreCase(property)) {
            return "speaker_id";
        }
        return property;
    }
}