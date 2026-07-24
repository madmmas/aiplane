package dev.madmmas.aimanager.usage.dto;

import java.math.BigDecimal;

/** Per-provider slice of a {@link UsageSummaryResponse}. */
public record UsageProviderBreakdownResponse(
    String provider, long requests, long inputTokens, long outputTokens, BigDecimal costUsd) {}
