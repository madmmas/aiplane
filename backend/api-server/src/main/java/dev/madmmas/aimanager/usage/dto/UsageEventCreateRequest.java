package dev.madmmas.aimanager.usage.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Single usage event within an ingest batch. Required wire fields: {@code projectId}, {@code
 * provider}, {@code model}, {@code status}. Numerics default to 0 when omitted; {@code costUsd}
 * omitted → computed from {@code CostRateRegistry}; an explicit value is kept as an override.
 * {@code timestamp} defaults to now; {@code id} is server-generated when absent.
 */
public record UsageEventCreateRequest(
    String id,
    String projectId,
    String promptId,
    String promptVersionId,
    String apiKeyId,
    String provider,
    String model,
    Integer inputTokens,
    Integer outputTokens,
    Integer latencyMs,
    BigDecimal costUsd,
    String status,
    Instant timestamp) {}
