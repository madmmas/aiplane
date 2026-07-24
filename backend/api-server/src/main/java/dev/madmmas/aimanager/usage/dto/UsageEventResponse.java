package dev.madmmas.aimanager.usage.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UsageEventResponse(
    String id,
    String projectId,
    String promptId,
    String promptVersionId,
    String apiKeyId,
    String provider,
    String model,
    int inputTokens,
    int outputTokens,
    int latencyMs,
    BigDecimal costUsd,
    String status,
    Instant timestamp) {}
