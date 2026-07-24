package dev.madmmas.aimanager.guardrail;

import java.time.Instant;
import java.util.Map;

/** Persisted guardrail row (Flyway V4). */
public record Guardrail(
    String id,
    String projectId,
    String name,
    GuardrailType type,
    GuardrailStage stage,
    Map<String, Object> config,
    boolean enabled,
    GuardrailAction action,
    String blockMessage,
    Instant createdAt) {

  public GuardrailRule toRule() {
    return new GuardrailRule(
        id, name, type, stage, config, enabled, action, blockMessage);
  }
}
