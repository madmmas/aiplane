package dev.madmmas.aimanager.guardrail.dto;

import java.time.Instant;
import java.util.Map;

public record GuardrailResponse(
    String id,
    String projectId,
    String name,
    String type,
    String stage,
    Map<String, Object> config,
    boolean enabled,
    String action,
    String blockMessage,
    Instant createdAt) {}
