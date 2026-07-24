package dev.madmmas.aimanager.guardrail.dto;

import java.time.Instant;
import java.util.List;

public record GuardrailSetResponse(
    String id,
    String projectId,
    String name,
    boolean shortCircuitOnBlock,
    List<String> guardrailIds,
    Instant createdAt) {}
