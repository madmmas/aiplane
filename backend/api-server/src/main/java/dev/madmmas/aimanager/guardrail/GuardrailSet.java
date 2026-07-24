package dev.madmmas.aimanager.guardrail;

import java.time.Instant;
import java.util.List;

/**
 * Named ordered bundle of guardrails (Flyway V5 + V10 {@code short_circuit_on_block}).
 *
 * @param guardrailIds member IDs in evaluation order (position ascending)
 */
public record GuardrailSet(
    String id,
    String projectId,
    String name,
    boolean shortCircuitOnBlock,
    List<String> guardrailIds,
    Instant createdAt) {

  public GuardrailSet {
    guardrailIds = guardrailIds == null ? List.of() : List.copyOf(guardrailIds);
  }
}
