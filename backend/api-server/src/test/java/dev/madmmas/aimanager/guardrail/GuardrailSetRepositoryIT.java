package dev.madmmas.aimanager.guardrail;

import static org.assertj.core.api.Assertions.assertThat;

import dev.madmmas.aimanager.support.AbstractPostgresIntegrationTest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class GuardrailSetRepositoryIT extends AbstractPostgresIntegrationTest {

  private static final String PROJECT_ID = "proj_news_radar";

  @Autowired private GuardrailRepository guardrailRepository;
  @Autowired private GuardrailSetRepository guardrailSetRepository;
  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void persistsOrderedMembersAndShortCircuitFlag() {
    assertThat(columnExists("guardrail_sets", "short_circuit_on_block")).isTrue();

    Guardrail keyword =
        guardrailRepository.save(
            new Guardrail(
                "gr_it_kw",
                PROJECT_ID,
                "it-keyword",
                GuardrailType.KEYWORD_BLOCKLIST,
                GuardrailStage.INPUT,
                Map.of("keywords", List.of("blocked")),
                true,
                GuardrailAction.BLOCK,
                null,
                Instant.now()));
    Guardrail length =
        guardrailRepository.save(
            new Guardrail(
                "gr_it_len",
                PROJECT_ID,
                "it-length",
                GuardrailType.MAX_LENGTH,
                GuardrailStage.BOTH,
                Map.of("maxChars", 40),
                true,
                GuardrailAction.WARN,
                null,
                Instant.now()));

    GuardrailSet saved =
        guardrailSetRepository.save(
            new GuardrailSet(
                "gs_it_1",
                PROJECT_ID,
                "it-production",
                true,
                List.of(length.id(), keyword.id()),
                Instant.now()));

    GuardrailSet loaded = guardrailSetRepository.findById(saved.id()).orElseThrow();
    assertThat(loaded.shortCircuitOnBlock()).isTrue();
    assertThat(loaded.guardrailIds()).containsExactly(length.id(), keyword.id());

    GuardrailSet reordered =
        guardrailSetRepository.update(
            new GuardrailSet(
                loaded.id(),
                loaded.projectId(),
                loaded.name(),
                false,
                List.of(keyword.id(), length.id()),
                loaded.createdAt()));

    assertThat(reordered.shortCircuitOnBlock()).isFalse();
    assertThat(reordered.guardrailIds()).containsExactly(keyword.id(), length.id());
  }

  private boolean columnExists(String table, String column) {
    Integer count =
        jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.columns
            WHERE table_name = ? AND column_name = ?
            """,
            Integer.class,
            table,
            column);
    return count != null && count > 0;
  }
}
