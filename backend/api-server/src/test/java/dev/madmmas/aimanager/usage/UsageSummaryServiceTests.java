package dev.madmmas.aimanager.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.usage.dto.UsageCostProjectionResponse;
import dev.madmmas.aimanager.usage.dto.UsageSummaryResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UsageSummaryServiceTests {

  private static final String PROJECT_ID = "proj_news_radar";
  private static final Instant NOW = Instant.parse("2026-07-24T12:00:00Z");

  @Mock private UsageEventRepository usageEventRepository;
  @Mock private ProjectRepository projectRepository;

  private UsageSummaryService service;

  @BeforeEach
  void setUp() {
    service =
        new UsageSummaryService(
            usageEventRepository, projectRepository, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void resolvePeriod7d() {
    UsageSummaryService.InstantRange range = UsageSummaryService.resolvePeriod("7d", NOW);
    assertThat(range.from()).isEqualTo(Instant.parse("2026-07-17T12:00:00Z"));
    assertThat(range.to()).isEqualTo(NOW);
  }

  @Test
  void resolvePeriodMonth() {
    UsageSummaryService.InstantRange range = UsageSummaryService.resolvePeriod("2026-07", NOW);
    assertThat(range.from()).isEqualTo(Instant.parse("2026-07-01T00:00:00Z"));
    assertThat(range.to()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
  }

  @Test
  void resolvePeriodRejectsGarbage() {
    assertThrows(
        IllegalArgumentException.class, () -> UsageSummaryService.resolvePeriod("last-week", NOW));
  }

  @Test
  void summaryAggregatesByProvider() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(usageEventRepository.aggregateByProvider(eq(PROJECT_ID), any(), any()))
        .thenReturn(
            List.of(
                new Object[] {
                  LlmProvider.ANTHROPIC, 2L, 100L, 200L, new BigDecimal("0.05000000")
                },
                new Object[] {LlmProvider.OPENAI, 1L, 10L, 20L, new BigDecimal("0.00100000")}));

    UsageSummaryResponse summary = service.summary(PROJECT_ID, "7d");

    assertThat(summary.projectId()).isEqualTo(PROJECT_ID);
    assertThat(summary.period()).isEqualTo("7d");
    assertThat(summary.totalRequests()).isEqualTo(3);
    assertThat(summary.totalInputTokens()).isEqualTo(110);
    assertThat(summary.totalOutputTokens()).isEqualTo(220);
    assertThat(summary.totalCostUsd()).isEqualByComparingTo("0.05100000");
    assertThat(summary.byProvider()).hasSize(2);
    assertThat(summary.byProvider().get(0).provider()).isEqualTo("anthropic");
  }

  @Test
  void summaryRejectsUnknownProject() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> service.summary(PROJECT_ID, "7d"));
  }

  @Test
  void projectionUsesSevenDayAverageTimesThirty() {
    when(projectRepository.existsById(PROJECT_ID)).thenReturn(true);
    when(usageEventRepository.sumCostUsd(eq(PROJECT_ID), any(), any()))
        .thenReturn(new BigDecimal("7.00000000"));

    UsageCostProjectionResponse projection = service.projection(PROJECT_ID);

    assertThat(projection.windowDays()).isEqualTo(7);
    assertThat(projection.avgDailyCostUsd()).isEqualByComparingTo("1.00000000");
    assertThat(projection.projectedMonthlyCostUsd()).isEqualByComparingTo("30.00000000");
    verify(usageEventRepository)
        .sumCostUsd(PROJECT_ID, Instant.parse("2026-07-17T12:00:00Z"), NOW);
  }
}
