package dev.madmmas.aimanager.guardrail;

import dev.madmmas.aimanager.guardrail.dto.EvaluatorResultResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateResponse;
import dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Runs ordered guardrail members against input/output text with optional short-circuit on the
 * first {@link GuardrailAction#BLOCK} failure.
 */
@Component
public class GuardrailSetEvaluationEngine {

  private final GuardrailEvaluatorRegistry registry;

  public GuardrailSetEvaluationEngine(GuardrailEvaluatorRegistry registry) {
    this.registry = registry;
  }

  public GuardrailSetEvaluateResponse evaluate(
      List<Guardrail> orderedMembers, String input, String output, boolean shortCircuitOnBlock) {
    List<EvaluatorResultResponse> results = new ArrayList<>();
    boolean blocked = false;
    boolean shortCircuited = false;

    for (Guardrail guardrail : orderedMembers) {
      if (!guardrail.enabled()) {
        continue;
      }

      if (guardrail.stage().appliesToInput()) {
        EvaluationResult result = registry.evaluate(guardrail.toRule(), input);
        results.add(GuardrailService.toEvaluatorResult(guardrail, GuardrailStage.INPUT, result));
        if (result.shouldBlock()) {
          blocked = true;
          if (shortCircuitOnBlock) {
            shortCircuited = true;
            break;
          }
        }
      }

      if (guardrail.stage().appliesToOutput()) {
        EvaluationResult result = registry.evaluate(guardrail.toRule(), output);
        results.add(GuardrailService.toEvaluatorResult(guardrail, GuardrailStage.OUTPUT, result));
        if (result.shouldBlock()) {
          blocked = true;
          if (shortCircuitOnBlock) {
            shortCircuited = true;
            break;
          }
        }
      }
    }

    return new GuardrailSetEvaluateResponse(blocked, shortCircuited, List.copyOf(results));
  }
}
