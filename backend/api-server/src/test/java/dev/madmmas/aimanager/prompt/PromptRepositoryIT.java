package dev.madmmas.aimanager.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import dev.madmmas.aimanager.support.AbstractPostgresIntegrationTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PromptRepositoryIT extends AbstractPostgresIntegrationTest {

  private static final String PROJECT_ID = "proj_news_radar";

  @Autowired private PromptRepository promptRepository;
  @Autowired private PromptVersionRepository versionRepository;
  @Autowired private EntityManager entityManager;

  @Test
  void persistsPromptWithTextArrayTags() {
    Prompt prompt = new Prompt();
    prompt.setId("prm_it_tags");
    prompt.setProjectId(PROJECT_ID);
    prompt.setName("it/tags-prompt");
    prompt.setDescription("tags round-trip");
    prompt.setTags(new String[] {"alpha", "beta"});

    Prompt saved = promptRepository.saveAndFlush(prompt);
    Prompt loaded = promptRepository.findById(saved.getId()).orElseThrow();

    assertThat(loaded.getTags()).containsExactly("alpha", "beta");
    assertThat(promptRepository.existsByProjectIdAndName(PROJECT_ID, "it/tags-prompt")).isTrue();
    assertThat(promptRepository.findByProjectIdOrderByUpdatedAtDesc(PROJECT_ID))
        .extracting(Prompt::getId)
        .contains("prm_it_tags");
  }

  @Test
  void persistsVersionWithJsonbAndAutoIncrementQuery() {
    Prompt prompt = new Prompt();
    prompt.setId("prm_it_ver");
    prompt.setProjectId(PROJECT_ID);
    prompt.setName("it/version-prompt");
    prompt.setTags(new String[0]);
    promptRepository.saveAndFlush(prompt);

    assertThat(versionRepository.findMaxVersionByPromptId("prm_it_ver")).isZero();

    PromptVersion v1 = newVersion("ver_it_1", "prm_it_ver", 1, "first");
    versionRepository.saveAndFlush(v1);

    PromptVersion v2 = newVersion("ver_it_2", "prm_it_ver", 2, "second");
    Map<String, Object> params = new HashMap<>();
    params.put("temperature", 0.4);
    params.put("maxTokens", 256);
    v2.setParameters(params);
    v2.setMetrics(Map.of("requestCount", 3, "avgLatencyMs", 120.5));
    versionRepository.saveAndFlush(v2);

    assertThat(versionRepository.findMaxVersionByPromptId("prm_it_ver")).isEqualTo(2);

    PromptVersion loaded =
        versionRepository.findByIdAndPromptId("ver_it_2", "prm_it_ver").orElseThrow();
    assertThat(loaded.getProvider()).isEqualTo(LlmProvider.ANTHROPIC);
    assertThat(loaded.getStatus()).isEqualTo(PromptVersionStatus.DRAFT);
    assertThat(loaded.getParameters()).containsEntry("temperature", 0.4);
    assertThat(loaded.getParameters()).containsEntry("maxTokens", 256);
    assertThat(loaded.getMetrics()).containsEntry("requestCount", 3);

    List<PromptVersion> versions =
        versionRepository.findByPromptIdOrderByVersionDesc("prm_it_ver");
    assertThat(versions).extracting(PromptVersion::getVersion).containsExactly(2, 1);
  }

  @Test
  void deletingPromptCascadesVersions() {
    Prompt prompt = new Prompt();
    prompt.setId("prm_it_cascade");
    prompt.setProjectId(PROJECT_ID);
    prompt.setName("it/cascade-prompt");
    prompt.setTags(new String[0]);
    promptRepository.saveAndFlush(prompt);
    versionRepository.saveAndFlush(newVersion("ver_it_cascade", "prm_it_cascade", 1, null));

    promptRepository.deleteById("prm_it_cascade");
    promptRepository.flush();
    // DB ON DELETE CASCADE removes versions; clear L1 cache so findById hits the DB.
    entityManager.clear();

    assertThat(promptRepository.findById("prm_it_cascade")).isEmpty();
    assertThat(versionRepository.findById("ver_it_cascade")).isEmpty();
  }

  private static PromptVersion newVersion(
      String id, String promptId, int version, String label) {
    PromptVersion entity = new PromptVersion();
    entity.setId(id);
    entity.setPromptId(promptId);
    entity.setVersion(version);
    entity.setLabel(label);
    entity.setModel("claude-haiku-4-5");
    entity.setProvider(LlmProvider.ANTHROPIC);
    entity.setSystemPrompt("sys");
    entity.setUserPromptTemplate("user {{x}}");
    entity.setParameters(Map.of("temperature", 0.0, "maxTokens", 100));
    entity.setStatus(PromptVersionStatus.DRAFT);
    entity.setCreatedBy("system");
    return entity;
  }
}
