package dev.madmmas.aimanager.prompt.dto;

public record PlaygroundRunResponse(
    String content,
    Integer inputTokens,
    Integer outputTokens,
    long latencyMs,
    String provider,
    String model,
    Boolean blockedByGuardrail) {}
