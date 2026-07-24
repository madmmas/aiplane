package dev.madmmas.aimanager.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.prompt.dto.PlaygroundRunRequest;
import dev.madmmas.aimanager.prompt.dto.PlaygroundRunResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaygroundServiceTests {

  private static final String PROMPT_ID = "prm_1";
  private static final String VERSION_ID = "ver_1";

  @Mock private PromptRepository promptRepository;
  @Mock private PromptVersionRepository versionRepository;
  @Mock private PromptPlaygroundRunner playgroundRunner;

  private PlaygroundService playgroundService;

  @BeforeEach
  void setUp() {
    playgroundService =
        new PlaygroundService(promptRepository, versionRepository, playgroundRunner);
  }

  @Test
  void runUsesActiveVersionWhenVersionIdOmitted() {
    Prompt prompt = promptWithActive(VERSION_ID);
    PromptVersion version = sampleVersion(VERSION_ID);
    when(promptRepository.findById(PROMPT_ID)).thenReturn(Optional.of(prompt));
    when(versionRepository.findByIdAndPromptId(VERSION_ID, PROMPT_ID))
        .thenReturn(Optional.of(version));
    when(playgroundRunner.run(any()))
        .thenReturn(new CompletionResult("hello", 10, 4, 42L, false));

    PlaygroundRunResponse response =
        playgroundService.run(
            PROMPT_ID,
            new PlaygroundRunRequest(
                null, Map.of("topic", "AI"), "openai", "gpt-4o-mini", 0.2, 256));

    assertThat(response.content()).isEqualTo("hello");
    assertThat(response.inputTokens()).isEqualTo(10);
    assertThat(response.outputTokens()).isEqualTo(4);
    assertThat(response.latencyMs()).isEqualTo(42L);
    assertThat(response.provider()).isEqualTo("openai");
    assertThat(response.model()).isEqualTo("gpt-4o-mini");
    assertThat(response.blockedByGuardrail()).isNull();

    ArgumentCaptor<PlaygroundCommand> captor = ArgumentCaptor.forClass(PlaygroundCommand.class);
    verify(playgroundRunner).run(captor.capture());
    PlaygroundCommand command = captor.getValue();
    assertThat(command.systemPrompt()).isEqualTo("You are helpful about AI.");
    assertThat(command.userPrompt()).isEqualTo("Explain AI briefly.");
    assertThat(command.provider()).isEqualTo(LlmProvider.OPENAI);
    assertThat(command.model()).isEqualTo("gpt-4o-mini");
    assertThat(command.temperature()).isEqualTo(0.2);
    assertThat(command.maxTokens()).isEqualTo(256);
  }

  @Test
  void runFallsBackToVersionParametersForTemperatureAndMaxTokens() {
    Prompt prompt = promptWithActive(VERSION_ID);
    PromptVersion version = sampleVersion(VERSION_ID);
    version.setParameters(Map.of("temperature", 0.7, "max_tokens", 512));
    when(promptRepository.findById(PROMPT_ID)).thenReturn(Optional.of(prompt));
    when(versionRepository.findByIdAndPromptId(VERSION_ID, PROMPT_ID))
        .thenReturn(Optional.of(version));
    when(playgroundRunner.run(any()))
        .thenReturn(new CompletionResult("ok", null, null, 5L, false));

    playgroundService.run(
        PROMPT_ID,
        new PlaygroundRunRequest(VERSION_ID, Map.of(), "anthropic", "claude-sonnet-4-0", null, null));

    ArgumentCaptor<PlaygroundCommand> captor = ArgumentCaptor.forClass(PlaygroundCommand.class);
    verify(playgroundRunner).run(captor.capture());
    assertThat(captor.getValue().temperature()).isEqualTo(0.7);
    assertThat(captor.getValue().maxTokens()).isEqualTo(512);
    assertThat(captor.getValue().provider()).isEqualTo(LlmProvider.ANTHROPIC);
  }

  @Test
  void runRequiresVersionWhenNoActiveVersion() {
    Prompt prompt = promptWithActive(null);
    when(promptRepository.findById(PROMPT_ID)).thenReturn(Optional.of(prompt));

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                playgroundService.run(
                    PROMPT_ID,
                    new PlaygroundRunRequest(
                        null, Map.of(), "openai", "gpt-4o-mini", null, null)));

    assertThat(error).hasMessageContaining("versionId is required");
  }

  @Test
  void runRejectsUnknownPrompt() {
    when(promptRepository.findById(PROMPT_ID)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () ->
            playgroundService.run(
                PROMPT_ID,
                new PlaygroundRunRequest(
                    VERSION_ID, Map.of(), "openai", "gpt-4o-mini", null, null)));
  }

  @Test
  void runRejectsUnknownProvider() {
    Prompt prompt = promptWithActive(VERSION_ID);
    when(promptRepository.findById(PROMPT_ID)).thenReturn(Optional.of(prompt));
    when(versionRepository.findByIdAndPromptId(VERSION_ID, PROMPT_ID))
        .thenReturn(Optional.of(sampleVersion(VERSION_ID)));

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                playgroundService.run(
                    PROMPT_ID,
                    new PlaygroundRunRequest(
                        VERSION_ID, Map.of(), "not-a-provider", "model", null, null)));

    assertThat(error).hasMessageContaining("Unknown LLM provider");
  }

  private static Prompt promptWithActive(String activeVersionId) {
    Prompt prompt = new Prompt();
    prompt.setId(PROMPT_ID);
    prompt.setProjectId("proj_1");
    prompt.setName("demo");
    prompt.setActiveVersionId(activeVersionId);
    return prompt;
  }

  private static PromptVersion sampleVersion(String id) {
    PromptVersion version = new PromptVersion();
    version.setId(id);
    version.setPromptId(PROMPT_ID);
    version.setVersion(1);
    version.setModel("gpt-4o-mini");
    version.setProvider(LlmProvider.OPENAI);
    version.setSystemPrompt("You are helpful about {{topic}}.");
    version.setUserPromptTemplate("Explain {{topic}} briefly.");
    version.setParameters(new HashMap<>());
    version.setStatus(PromptVersionStatus.ACTIVE);
    version.setCreatedBy("system");
    return version;
  }
}
