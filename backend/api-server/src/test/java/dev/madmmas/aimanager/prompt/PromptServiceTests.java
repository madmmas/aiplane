package dev.madmmas.aimanager.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.prompt.dto.PromptCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptResponse;
import dev.madmmas.aimanager.prompt.dto.PromptUpdateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromptServiceTests {

  private static final String PROJECT_ID = "proj_news_radar";

  @Mock private PromptRepository promptRepository;
  @Mock private PromptVersionRepository versionRepository;
  @Mock private ProjectRepository projectRepository;

  @InjectMocks private PromptService promptService;

  @Test
  void listRequiresProjectId() {
    IllegalArgumentException error =
        assertThrows(IllegalArgumentException.class, () -> promptService.list("  "));
    assertThat(error).hasMessageContaining("projectId is required");
  }

  @Test
  void createRejectsUnknownProject() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(false);

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                promptService.create(
                    new PromptCreateRequest(PROJECT_ID, "news-radar/dedup", null, null)));

    assertThat(error).hasMessageContaining("Unknown projectId");
    verify(promptRepository, never()).save(any());
  }

  @Test
  void createPersistsPromptWithTags() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(promptRepository.existsByProjectIdAndName(PROJECT_ID, "news-radar/dedup"))
        .thenReturn(false);
    when(promptRepository.save(any(Prompt.class)))
        .thenAnswer(
            invocation -> {
              Prompt saved = invocation.getArgument(0);
              saved.setCreatedAt(Instant.parse("2026-07-24T12:00:00Z"));
              saved.setUpdatedAt(Instant.parse("2026-07-24T12:00:00Z"));
              return saved;
            });

    PromptResponse response =
        promptService.create(
            new PromptCreateRequest(
                PROJECT_ID, "news-radar/dedup", "Dedup judge", List.of("rag", "judge")));

    ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
    verify(promptRepository).save(captor.capture());
    Prompt saved = captor.getValue();
    assertThat(saved.getId()).startsWith("prm_");
    assertThat(saved.getProjectId()).isEqualTo(PROJECT_ID);
    assertThat(saved.getName()).isEqualTo("news-radar/dedup");
    assertThat(saved.getTags()).containsExactly("rag", "judge");
    assertThat(response.tags()).containsExactly("rag", "judge");
  }

  @Test
  void createRejectsDuplicateName() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(promptRepository.existsByProjectIdAndName(PROJECT_ID, "dup")).thenReturn(true);

    assertThrows(
        IllegalArgumentException.class,
        () -> promptService.create(new PromptCreateRequest(PROJECT_ID, "dup", null, null)));
  }

  @Test
  void updatePatchesNameDescriptionAndTags() {
    Prompt existing = prompt("prm_1", "old-name", "old desc", new String[] {"a"});
    when(promptRepository.findById("prm_1")).thenReturn(Optional.of(existing));
    when(promptRepository.existsByProjectIdAndNameAndIdNot(PROJECT_ID, "new-name", "prm_1"))
        .thenReturn(false);
    when(promptRepository.save(existing)).thenReturn(existing);

    PromptResponse response =
        promptService.update(
            "prm_1",
            new PromptUpdateRequest("new-name", "new desc", List.of("b", "c")));

    assertThat(existing.getName()).isEqualTo("new-name");
    assertThat(existing.getDescription()).isEqualTo("new desc");
    assertThat(existing.getTags()).containsExactly("b", "c");
    assertThat(response.name()).isEqualTo("new-name");
  }

  @Test
  void getThrowsWhenMissing() {
    when(promptRepository.findById("missing")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> promptService.get("missing"));
  }

  @Test
  void deleteThrowsWhenMissing() {
    when(promptRepository.existsById("missing")).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> promptService.delete("missing"));
    verify(promptRepository, never()).deleteById(any());
  }

  @Test
  void createVersionAutoIncrementsAndForcesDraft() {
    Prompt prompt = prompt("prm_1", "p", null, new String[0]);
    when(promptRepository.findById("prm_1")).thenReturn(Optional.of(prompt));
    when(versionRepository.findMaxVersionByPromptId("prm_1")).thenReturn(2);
    when(versionRepository.save(any(PromptVersion.class)))
        .thenAnswer(
            invocation -> {
              PromptVersion saved = invocation.getArgument(0);
              saved.setCreatedAt(Instant.parse("2026-07-24T12:00:00Z"));
              return saved;
            });

    PromptVersionResponse response =
        promptService.createVersion(
            "prm_1",
            new PromptVersionCreateRequest(
                "haiku",
                "claude-haiku-4-5",
                "anthropic",
                "You are helpful.",
                "Summarize {{text}}",
                Map.of("temperature", 0.2, "maxTokens", 512),
                null));

    ArgumentCaptor<PromptVersion> captor = ArgumentCaptor.forClass(PromptVersion.class);
    verify(versionRepository).save(captor.capture());
    PromptVersion saved = captor.getValue();
    assertThat(saved.getId()).startsWith("ver_");
    assertThat(saved.getVersion()).isEqualTo(3);
    assertThat(saved.getStatus()).isEqualTo(PromptVersionStatus.DRAFT);
    assertThat(saved.getCreatedBy()).isEqualTo("system");
    assertThat(saved.getProvider()).isEqualTo(LlmProvider.ANTHROPIC);
    assertThat(response.status()).isEqualTo("draft");
    assertThat(response.provider()).isEqualTo("anthropic");
    assertThat(response.version()).isEqualTo(3);
  }

  @Test
  void createVersionRejectsUnknownProvider() {
    when(promptRepository.findById("prm_1"))
        .thenReturn(Optional.of(prompt("prm_1", "p", null, new String[0])));

    assertThrows(
        IllegalArgumentException.class,
        () ->
            promptService.createVersion(
                "prm_1",
                new PromptVersionCreateRequest(
                    null, "model", "not-a-provider", null, null, null, "user_1")));
    verify(versionRepository, never()).save(any());
  }

  @Test
  void getVersionScopedToPrompt() {
    when(promptRepository.findById("prm_1"))
        .thenReturn(Optional.of(prompt("prm_1", "p", null, new String[0])));
    when(versionRepository.findByIdAndPromptId(eq("ver_1"), eq("prm_1")))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class, () -> promptService.getVersion("prm_1", "ver_1"));
  }

  private static Prompt prompt(String id, String name, String description, String[] tags) {
    Prompt prompt = new Prompt();
    prompt.setId(id);
    prompt.setProjectId(PROJECT_ID);
    prompt.setName(name);
    prompt.setDescription(description);
    prompt.setTags(tags);
    prompt.setCreatedAt(Instant.parse("2026-07-24T10:00:00Z"));
    prompt.setUpdatedAt(Instant.parse("2026-07-24T10:00:00Z"));
    return prompt;
  }
}
