package dev.madmmas.aimanager.usage;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.provider.CostRateRegistry;
import dev.madmmas.aimanager.usage.dto.UsageCostProjectionResponse;
import dev.madmmas.aimanager.usage.dto.UsageEventResponse;
import dev.madmmas.aimanager.usage.dto.UsageProviderBreakdownResponse;
import dev.madmmas.aimanager.usage.dto.UsageSummaryResponse;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageSummaryService {

  static final int EVENTS_LIMIT = 500;
  static final int PROJECTION_WINDOW_DAYS = 7;
  static final int PROJECTION_MONTH_DAYS = 30;

  private static final Pattern RELATIVE_PERIOD = Pattern.compile("^(\\d+)d$");
  private static final Pattern MONTH_PERIOD = Pattern.compile("^\\d{4}-\\d{2}$");

  private final UsageEventRepository usageEventRepository;
  private final ProjectRepository projectRepository;
  private final Clock clock;

  @Autowired
  public UsageSummaryService(
      UsageEventRepository usageEventRepository, ProjectRepository projectRepository) {
    this(usageEventRepository, projectRepository, Clock.systemUTC());
  }

  UsageSummaryService(
      UsageEventRepository usageEventRepository, ProjectRepository projectRepository, Clock clock) {
    this.usageEventRepository = usageEventRepository;
    this.projectRepository = projectRepository;
    this.clock = clock;
  }

  @Transactional(readOnly = true)
  public UsageSummaryResponse summary(String projectId, String period) {
    requireProject(projectId);
    if (period == null || period.isBlank()) {
      throw new IllegalArgumentException("period is required (7d, 30d, or yyyy-MM)");
    }
    String normalized = period.trim();
    InstantRange range = resolvePeriod(normalized, Instant.now(clock));

    List<Object[]> rows =
        usageEventRepository.aggregateByProvider(projectId, range.from(), range.to());

    List<UsageProviderBreakdownResponse> byProvider = new ArrayList<>(rows.size());
    long totalRequests = 0;
    long totalInput = 0;
    long totalOutput = 0;
    BigDecimal totalCost = BigDecimal.ZERO.setScale(CostRateRegistry.COST_SCALE);

    for (Object[] row : rows) {
      LlmProvider provider = (LlmProvider) row[0];
      long requests = ((Number) row[1]).longValue();
      long inputTokens = ((Number) row[2]).longValue();
      long outputTokens = ((Number) row[3]).longValue();
      BigDecimal cost =
          ((BigDecimal) row[4]).setScale(CostRateRegistry.COST_SCALE, CostRateRegistry.COST_ROUNDING);

      byProvider.add(
          new UsageProviderBreakdownResponse(
              provider.wireValue(), requests, inputTokens, outputTokens, cost));

      totalRequests += requests;
      totalInput += inputTokens;
      totalOutput += outputTokens;
      totalCost = totalCost.add(cost);
    }

    return new UsageSummaryResponse(
        projectId,
        normalized,
        totalRequests,
        totalInput,
        totalOutput,
        totalCost.setScale(CostRateRegistry.COST_SCALE, CostRateRegistry.COST_ROUNDING),
        byProvider);
  }

  @Transactional(readOnly = true)
  public List<UsageEventResponse> listEvents(String projectId, Instant from, Instant to) {
    requireProject(projectId);
    if (from == null || to == null) {
      throw new IllegalArgumentException("from and to are required (ISO-8601 instants)");
    }
    if (from.isAfter(to)) {
      throw new IllegalArgumentException("from must be <= to");
    }

    return usageEventRepository
        .findByProjectIdAndTimestampGreaterThanEqualAndTimestampLessThanEqualOrderByTimestampDesc(
            projectId, from, to, PageRequest.of(0, EVENTS_LIMIT))
        .stream()
        .map(UsageService::toResponse)
        .toList();
  }

  /**
   * Monthly projection: {@code (sum cost over last 7d) / 7 * 30}. Empty window → zeros.
   */
  @Transactional(readOnly = true)
  public UsageCostProjectionResponse projection(String projectId) {
    requireProject(projectId);
    Instant to = Instant.now(clock);
    Instant from = to.minus(PROJECTION_WINDOW_DAYS, ChronoUnit.DAYS);

    BigDecimal raw = usageEventRepository.sumCostUsd(projectId, from, to);
    BigDecimal windowCost =
        (raw == null ? BigDecimal.ZERO : raw)
            .setScale(CostRateRegistry.COST_SCALE, CostRateRegistry.COST_ROUNDING);

    BigDecimal avgDaily =
        windowCost.divide(
            BigDecimal.valueOf(PROJECTION_WINDOW_DAYS),
            CostRateRegistry.COST_SCALE,
            CostRateRegistry.COST_ROUNDING);
    BigDecimal projected =
        avgDaily
            .multiply(BigDecimal.valueOf(PROJECTION_MONTH_DAYS))
            .setScale(CostRateRegistry.COST_SCALE, CostRateRegistry.COST_ROUNDING);

    return new UsageCostProjectionResponse(
        projectId, PROJECTION_WINDOW_DAYS, avgDaily, projected);
  }

  private void requireProject(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      throw new IllegalArgumentException("projectId is required");
    }
    if (!projectRepository.existsById(projectId.trim())) {
      throw new ResourceNotFoundException("Unknown projectId: " + projectId.trim());
    }
  }

  static InstantRange resolvePeriod(String period, Instant now) {
    var relative = RELATIVE_PERIOD.matcher(period);
    if (relative.matches()) {
      int days = Integer.parseInt(relative.group(1));
      if (days <= 0) {
        throw new IllegalArgumentException("period day count must be > 0");
      }
      Instant to = now;
      Instant from = now.minus(days, ChronoUnit.DAYS);
      return new InstantRange(from, to);
    }

    if (MONTH_PERIOD.matcher(period).matches()) {
      YearMonth month;
      try {
        month = YearMonth.parse(period);
      } catch (DateTimeParseException ex) {
        throw new IllegalArgumentException("Invalid period month: " + period);
      }
      LocalDate start = month.atDay(1);
      Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
      Instant to = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
      return new InstantRange(from, to);
    }

    throw new IllegalArgumentException("period must be 7d, 30d, or yyyy-MM");
  }

  record InstantRange(Instant from, Instant to) {}
}
