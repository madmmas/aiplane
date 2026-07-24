package dev.madmmas.aimanager.usage.dto;

import java.math.BigDecimal;

/**
 * Lean monthly cost estimate: average daily spend over the last 7 days × 30.
 *
 * @param projectId project scope
 * @param windowDays lookback window used for the average (always 7 for MVP)
 * @param avgDailyCostUsd mean daily cost in the window
 * @param projectedMonthlyCostUsd {@code avgDailyCostUsd * 30}
 */
public record UsageCostProjectionResponse(
    String projectId,
    int windowDays,
    BigDecimal avgDailyCostUsd,
    BigDecimal projectedMonthlyCostUsd) {}
