package dev.madmmas.aimanager.guardrail;

import java.util.Map;

/**
 * In-memory guardrail definition used by evaluators and the Spring AI advisor.
 *
 * <p>Persistence / CRUD lands in #55; this shape mirrors the V4 {@code guardrails} row.
 */
public record GuardrailRule(
    String id,
    String name,
    GuardrailType type,
    GuardrailStage stage,
    Map<String, Object> config,
    boolean enabled,
    GuardrailAction action,
    String blockMessage) {

  public GuardrailRule {
    if (config == null) {
      config = Map.of();
    } else {
      config = Map.copyOf(config);
    }
  }
}
