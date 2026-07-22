package dev.madmmas.aimanager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AiManagerApplicationTests {

  static {
    // docker-java defaults to API 1.24; Docker Engine 25+/Desktop rejects that with HTTP 400.
    System.setProperty("api.version", "1.44");
  }

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void contextLoads() {
    // Spring context starts with Flyway against Testcontainers Postgres.
  }

  @Test
  void healthEndpointIsUp() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
  }

  @Test
  void flywayMigrationsCreateCoreTablesAndSeed() {
    List<String> tables =
        jdbcTemplate.queryForList(
            """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
              AND table_type = 'BASE TABLE'
              AND table_name IN (
                'projects', 'prompts', 'prompt_versions', 'guardrails',
                'guardrail_sets', 'guardrail_set_members', 'users',
                'project_memberships', 'api_keys', 'usage_events', 'config_properties'
              )
            ORDER BY table_name
            """,
            String.class);

    assertThat(tables)
        .containsExactly(
            "api_keys",
            "config_properties",
            "guardrail_set_members",
            "guardrail_sets",
            "guardrails",
            "project_memberships",
            "projects",
            "prompt_versions",
            "prompts",
            "usage_events",
            "users");

    Integer projectCount =
        jdbcTemplate.queryForObject("SELECT COUNT(*) FROM projects", Integer.class);
    Integer adminCount =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?", Integer.class, "admin@aiplane.local");

    assertThat(projectCount).isGreaterThanOrEqualTo(2);
    assertThat(adminCount).isEqualTo(1);

    List<Map<String, Object>> flyway =
        jdbcTemplate.queryForList(
            "SELECT version, success FROM flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank");
    assertThat(flyway).extracting(row -> row.get("version")).contains("1", "2", "3", "9");
    assertThat(flyway).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));
  }
}
