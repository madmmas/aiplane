package dev.madmmas.aimanager.prompt.dto;

import java.time.Instant;
import java.util.Map;

public record PromptVersionResponse(
    String id,
    String promptId,
    int version,
    String label,
    String model,
    String provider,
    String systemPrompt,
    String userPromptTemplate,
    Map<String, Object> parameters,
    String status,
    String createdBy,
    Instant createdAt,
    Map<String, Object> metrics) {}
