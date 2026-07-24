package dev.madmmas.aimanager.guardrail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcGuardrailRepository implements GuardrailRepository {

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

  private final JdbcTemplate jdbcTemplate;
  private final ObjectMapper objectMapper;
  private final RowMapper<Guardrail> rowMapper = this::mapRow;

  public JdbcGuardrailRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public Guardrail save(Guardrail guardrail) {
    jdbcTemplate.update(
        """
        INSERT INTO guardrails
          (id, project_id, name, type, stage, config, enabled, action, block_message, created_at)
        VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
        """,
        guardrail.id(),
        guardrail.projectId(),
        guardrail.name(),
        guardrail.type().wireValue(),
        guardrail.stage().wireValue(),
        toJson(guardrail.config()),
        guardrail.enabled(),
        guardrail.action().wireValue(),
        guardrail.blockMessage(),
        Timestamp.from(guardrail.createdAt()));
    return guardrail;
  }

  @Override
  public Optional<Guardrail> findById(String id) {
    try {
      return Optional.of(
          jdbcTemplate.queryForObject(
              """
              SELECT id, project_id, name, type, stage, config::text AS config, enabled, action,
                     block_message, created_at
              FROM guardrails WHERE id = ?
              """,
              rowMapper,
              id));
    } catch (EmptyResultDataAccessException ex) {
      return Optional.empty();
    }
  }

  @Override
  public List<Guardrail> findByProjectId(String projectId) {
    return jdbcTemplate.query(
        """
        SELECT id, project_id, name, type, stage, config::text AS config, enabled, action,
               block_message, created_at
        FROM guardrails WHERE project_id = ? ORDER BY name
        """,
        rowMapper,
        projectId);
  }

  @Override
  public List<Guardrail> findByIds(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    String placeholders = String.join(",", ids.stream().map(id -> "?").toList());
    List<Guardrail> found =
        jdbcTemplate.query(
            """
            SELECT id, project_id, name, type, stage, config::text AS config, enabled, action,
                   block_message, created_at
            FROM guardrails WHERE id IN (%s)
            """
                .formatted(placeholders),
            rowMapper,
            ids.toArray());
    Map<String, Guardrail> byId =
        found.stream().collect(java.util.stream.Collectors.toMap(Guardrail::id, g -> g));
    return ids.stream().map(byId::get).filter(java.util.Objects::nonNull).toList();
  }

  @Override
  public Guardrail update(Guardrail guardrail) {
    int updated =
        jdbcTemplate.update(
            """
            UPDATE guardrails
            SET name = ?, type = ?, stage = ?, config = ?::jsonb, enabled = ?, action = ?,
                block_message = ?
            WHERE id = ?
            """,
            guardrail.name(),
            guardrail.type().wireValue(),
            guardrail.stage().wireValue(),
            toJson(guardrail.config()),
            guardrail.enabled(),
            guardrail.action().wireValue(),
            guardrail.blockMessage(),
            guardrail.id());
    if (updated == 0) {
      throw new IllegalStateException("Guardrail not found: " + guardrail.id());
    }
    return guardrail;
  }

  @Override
  public boolean deleteById(String id) {
    return jdbcTemplate.update("DELETE FROM guardrails WHERE id = ?", id) > 0;
  }

  @Override
  public boolean existsByProjectIdAndName(String projectId, String name) {
    Long count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guardrails WHERE project_id = ? AND name = ?",
            Long.class,
            projectId,
            name);
    return count != null && count > 0;
  }

  private Guardrail mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new Guardrail(
        rs.getString("id"),
        rs.getString("project_id"),
        rs.getString("name"),
        GuardrailType.fromWireValue(rs.getString("type")),
        GuardrailStage.fromWireValue(rs.getString("stage")),
        fromJson(rs.getString("config")),
        rs.getBoolean("enabled"),
        GuardrailAction.fromWireValue(rs.getString("action")),
        rs.getString("block_message"),
        toInstant(rs.getTimestamp("created_at")));
  }

  private String toJson(Map<String, Object> config) {
    try {
      return objectMapper.writeValueAsString(config == null ? Map.of() : config);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Invalid guardrail config JSON", ex);
    }
  }

  private Map<String, Object> fromJson(String json) {
    try {
      if (json == null || json.isBlank()) {
        return Map.of();
      }
      return objectMapper.readValue(json, MAP_TYPE);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("Corrupt guardrail config JSON", ex);
    }
  }

  private static Instant toInstant(Timestamp timestamp) {
    return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
  }
}
