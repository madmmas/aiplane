package dev.madmmas.aimanager.guardrail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcGuardrailSetRepository implements GuardrailSetRepository {

  private final JdbcTemplate jdbcTemplate;

  public JdbcGuardrailSetRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public GuardrailSet save(GuardrailSet set) {
    jdbcTemplate.update(
        """
        INSERT INTO guardrail_sets (id, project_id, name, short_circuit_on_block, created_at)
        VALUES (?, ?, ?, ?, ?)
        """,
        set.id(),
        set.projectId(),
        set.name(),
        set.shortCircuitOnBlock(),
        Timestamp.from(set.createdAt()));
    replaceMembers(set.id(), set.guardrailIds());
    return set;
  }

  @Override
  public Optional<GuardrailSet> findById(String id) {
    try {
      GuardrailSetHeader header =
          jdbcTemplate.queryForObject(
              """
              SELECT id, project_id, name, short_circuit_on_block, created_at
              FROM guardrail_sets WHERE id = ?
              """,
              this::mapHeader,
              id);
      if (header == null) {
        return Optional.empty();
      }
      return Optional.of(toSet(header, loadMemberIds(id)));
    } catch (EmptyResultDataAccessException ex) {
      return Optional.empty();
    }
  }

  @Override
  public List<GuardrailSet> findByProjectId(String projectId) {
    List<GuardrailSetHeader> headers =
        jdbcTemplate.query(
            """
            SELECT id, project_id, name, short_circuit_on_block, created_at
            FROM guardrail_sets WHERE project_id = ? ORDER BY name
            """,
            this::mapHeader,
            projectId);
    List<GuardrailSet> sets = new ArrayList<>(headers.size());
    for (GuardrailSetHeader header : headers) {
      sets.add(toSet(header, loadMemberIds(header.id())));
    }
    return sets;
  }

  @Override
  @Transactional
  public GuardrailSet update(GuardrailSet set) {
    int updated =
        jdbcTemplate.update(
            """
            UPDATE guardrail_sets
            SET name = ?, short_circuit_on_block = ?
            WHERE id = ?
            """,
            set.name(),
            set.shortCircuitOnBlock(),
            set.id());
    if (updated == 0) {
      throw new IllegalStateException("Guardrail set not found: " + set.id());
    }
    replaceMembers(set.id(), set.guardrailIds());
    return set;
  }

  @Override
  @Transactional
  public boolean deleteById(String id) {
    return jdbcTemplate.update("DELETE FROM guardrail_sets WHERE id = ?", id) > 0;
  }

  @Override
  public boolean existsByProjectIdAndName(String projectId, String name) {
    Long count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM guardrail_sets WHERE project_id = ? AND name = ?",
            Long.class,
            projectId,
            name);
    return count != null && count > 0;
  }

  private void replaceMembers(String setId, List<String> guardrailIds) {
    jdbcTemplate.update("DELETE FROM guardrail_set_members WHERE guardrail_set_id = ?", setId);
    int position = 0;
    for (String guardrailId : guardrailIds) {
      jdbcTemplate.update(
          """
          INSERT INTO guardrail_set_members (guardrail_set_id, guardrail_id, position)
          VALUES (?, ?, ?)
          """,
          setId,
          guardrailId,
          position++);
    }
  }

  private List<String> loadMemberIds(String setId) {
    return jdbcTemplate.queryForList(
        """
        SELECT guardrail_id FROM guardrail_set_members
        WHERE guardrail_set_id = ?
        ORDER BY position ASC, guardrail_id ASC
        """,
        String.class,
        setId);
  }

  private GuardrailSetHeader mapHeader(ResultSet rs, int rowNum) throws SQLException {
    return new GuardrailSetHeader(
        rs.getString("id"),
        rs.getString("project_id"),
        rs.getString("name"),
        rs.getBoolean("short_circuit_on_block"),
        rs.getTimestamp("created_at").toInstant());
  }

  private static GuardrailSet toSet(GuardrailSetHeader header, List<String> memberIds) {
    return new GuardrailSet(
        header.id(),
        header.projectId(),
        header.name(),
        header.shortCircuitOnBlock(),
        memberIds,
        header.createdAt());
  }

  private record GuardrailSetHeader(
      String id,
      String projectId,
      String name,
      boolean shortCircuitOnBlock,
      Instant createdAt) {}
}
