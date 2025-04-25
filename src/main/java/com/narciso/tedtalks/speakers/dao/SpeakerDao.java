package com.narciso.tedtalks.speakers.dao;

import com.narciso.tedtalks.speakers.domain.Speaker;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
@Repository
public class SpeakerDao {
    private final NamedParameterJdbcTemplate jdbc;
    private final SpeakerRowMapper speakerRowMapper = new SpeakerRowMapper();

    public SpeakerDao(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Speaker save(Speaker speaker) {
        var sql = "INSERT INTO speakers (name) VALUES (:name)";
        var params = new MapSqlParameterSource()
                .addValue("name", speaker.getName());
        jdbc.update(sql, params);

        Long id = jdbc.queryForObject(
                "SELECT id FROM speakers WHERE name = :name", params, Long.class);

        speaker.setId(id);
        return speaker;
    }

    public Optional<Speaker> findByName(String name) {
        String sql = "SELECT * FROM speakers WHERE name = :name";
        var params = new MapSqlParameterSource().addValue("name", name);
        List<Speaker> list = jdbc.query(sql, params, new SpeakerRowMapper());
        return list.stream().findFirst();
    }

    public Optional<Speaker> findById(Long id) {
        String sql = "SELECT * FROM speakers WHERE id = :id";
        var params = new MapSqlParameterSource().addValue("id", id);
        List<Speaker> list = jdbc.query(sql, params, new SpeakerRowMapper());
        return list.stream().findFirst();
    }

    public Page<Speaker> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM speakers";
        Long total = jdbc.queryForObject(countSql, new MapSqlParameterSource(), Long.class);
        long totalElements = (total != null) ? total : 0L;

        String sortClause = buildSortClause(pageable.getSort());

        String dataSql = "SELECT * FROM speakers " + sortClause + " LIMIT :limit OFFSET :offset";

        var params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        List<Speaker> content = jdbc.query(dataSql, params, speakerRowMapper);

        return new PageImpl<>(content, pageable, totalElements);
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
                sortClause.append(property).append(" ").append(order.getDirection().name());
                first = false;
            }
            if (first) {
                sortClause.append("name ASC");
            }
        } else {
            sortClause.append("name ASC");
        }
        return sortClause.toString();
    }

    private boolean isValidSortProperty(String property) {
        return property != null && (property.equalsIgnoreCase("id") || property.equalsIgnoreCase("name"));
    }

    public void update(Speaker speaker) {
        String sql = "UPDATE speakers SET name = :name WHERE id = :id";
        jdbc.update(sql, Map.of("name", speaker.getName(), "id", speaker.getId()));
    }

    public void delete(Long id) {
        jdbc.update("DELETE FROM speakers WHERE id = :id", Map.of("id", id));
    }

}
