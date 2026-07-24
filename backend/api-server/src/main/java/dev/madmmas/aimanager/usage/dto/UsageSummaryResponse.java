package dev.madmmas.aimanager.usage.dto;

import java.math.BigDecimal;
import java.util.List;

/** Aggregated usage for a project over a period ({@code 7d}, {@code 30d}, or {@code yyyy-MM}). */
public record UsageSummaryResponse(
    String projectId,
    String period,
    long totalRequests,
    long totalInputTokens,
    long totalOutputTokens,
    BigDecimal totalCostUsd,
    List<UsageProviderBreakdownResponse> byProvider) {}
