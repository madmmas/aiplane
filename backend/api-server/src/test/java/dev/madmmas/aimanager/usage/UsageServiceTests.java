package dev.madmmas.aimanager.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.common.exception.BatchValidationException;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.usage.dto.UsageEventCreateRequest;
import dev.madmmas.aimanager.usage.dto.UsageEventIngestRequest;
import dev.madmmas.aimanager.usage.dto.UsageEventIngestResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageServiceTests {

  private static final String PROJECT_ID = "proj_news_radar";

  @Mock private UsageEventRepository usageEventRepository;
  @Mock private ProjectRepository projectRepository;

  private UsageService usageService;

  @BeforeEach
  void setUp() {
    usageService = new UsageService(usageEventRepository, projectRepository);
  }

  @Test
  void ingestRejectsEmptyBatch() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () -> usageService.ingest(new UsageEventIngestRequest(List.of())));
    assertThat(error).hasMessageContaining("non-empty");
    verify(usageEventRepository, never()).saveAll(anyList());
  }

  @Test
  void ingestRejectsNullEvents() {
    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class,
            () -> usageService.ingest(new UsageEventIngestRequest(null)));
    assertThat(error).hasMessageContaining("non-empty");
  }

  @Test
  void ingestRejectsUnknownProjectAllOrNothing() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(false);

    BatchValidationException error =
        assertThrows(
            BatchValidationException.class,
            () ->
                usageService.ingest(
                    new UsageEventIngestRequest(
                        List.of(
                            event(
                                PROJECT_ID,
                                "anthropic",
                                "claude-sonnet",
                                "success",
                                10,
                                20,
                                100,
                                BigDecimal.ZERO),
                            event(
                                PROJECT_ID,
                                "openai",
                                "gpt-4o",
                                "error",
                                1,
                                2,
                                50,
                                BigDecimal.ONE)))));

    assertThat(error.getErrors()).hasSize(2);
    assertThat(error.getErrors().get(0)).contains("Unknown projectId");
    verify(usageEventRepository, never()).saveAll(anyList());
  }

  @Test
  void ingestCollectsMultipleFieldErrors() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);

    BatchValidationException error =
        assertThrows(
            BatchValidationException.class,
            () ->
                usageService.ingest(
                    new UsageEventIngestRequest(
                        List.of(
                            event(
                                PROJECT_ID,
                                "not-a-provider",
                                "model",
                                "success",
                                1,
                                1,
                                1,
                                BigDecimal.ZERO),
                            event(
                                PROJECT_ID,
                                "openai",
                                "gpt",
                                "nope",
                                -1,
                                1,
                                1,
                                BigDecimal.ZERO)))));

    assertThat(error.getErrors()).hasSize(2);
    assertThat(error.getErrors().get(0)).contains("Unknown LLM provider");
    assertThat(error.getErrors().get(1)).containsAnyOf("Unknown usage event status", "inputTokens");
    verify(usageEventRepository, never()).saveAll(anyList());
  }

  @Test
  void ingestRejectsNegativeNumerics() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);

    BatchValidationException error =
        assertThrows(
            BatchValidationException.class,
            () ->
                usageService.ingest(
                    new UsageEventIngestRequest(
                        List.of(
                            event(
                                PROJECT_ID,
                                "openai",
                                "gpt-4o",
                                "success",
                                1,
                                1,
                                1,
                                new BigDecimal("-0.01"))))));

    assertThat(error.getErrors()).anyMatch(e -> e.contains("costUsd must be >= 0"));
  }

  @Test
  void ingestPersistsValidBatch() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(usageEventRepository.saveAll(anyList()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Instant ts = Instant.parse("2026-07-24T12:00:00Z");
    UsageEventIngestResponse response =
        usageService.ingest(
            new UsageEventIngestRequest(
                List.of(
                    new UsageEventCreateRequest(
                        null,
                        PROJECT_ID,
                        null,
                        null,
                        null,
                        "anthropic",
                        "claude-sonnet-4-20250514",
                        12,
                        34,
                        90,
                        new BigDecimal("0.0012"),
                        "success",
                        ts))));

    assertThat(response.accepted()).isEqualTo(1);
    assertThat(response.events()).hasSize(1);
    assertThat(response.events().get(0).id()).startsWith("ue_");
    assertThat(response.events().get(0).provider()).isEqualTo("anthropic");
    assertThat(response.events().get(0).status()).isEqualTo("success");
    assertThat(response.events().get(0).timestamp()).isEqualTo(ts);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<UsageEvent>> captor = ArgumentCaptor.forClass(List.class);
    verify(usageEventRepository).saveAll(captor.capture());
    assertThat(captor.getValue()).hasSize(1);
  }

  private static UsageEventCreateRequest event(
      String projectId,
      String provider,
      String model,
      String status,
      Integer inputTokens,
      Integer outputTokens,
      Integer latencyMs,
      BigDecimal costUsd) {
    return new UsageEventCreateRequest(
        null,
        projectId,
        null,
        null,
        null,
        provider,
        model,
        inputTokens,
        outputTokens,
        latencyMs,
        costUsd,
        status,
        null);
  }
}
