package dev.madmmas.aimanager.guardrail.evaluator;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailEvaluator;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Resolves a {@link GuardrailEvaluator} by {@link GuardrailType}. */
@Component
public class GuardrailEvaluatorRegistry {

  private final Map<GuardrailType, GuardrailEvaluator> byType;

  public GuardrailEvaluatorRegistry(List<GuardrailEvaluator> evaluators) {
    Map<GuardrailType, GuardrailEvaluator> map = new EnumMap<>(GuardrailType.class);
    for (GuardrailEvaluator evaluator : evaluators) {
      GuardrailType type = evaluator.supportedType();
      if (map.containsKey(type)) {
        throw new IllegalStateException("Duplicate GuardrailEvaluator for type " + type);
      }
      map.put(type, evaluator);
    }
    this.byType = Map.copyOf(map);
  }

  public GuardrailEvaluator require(GuardrailType type) {
    GuardrailEvaluator evaluator = byType.get(type);
    if (evaluator == null) {
      throw new IllegalArgumentException("No evaluator registered for type: " + type.wireValue());
    }
    return evaluator;
  }

  public EvaluationResult evaluate(GuardrailRule rule, String text) {
    if (!rule.enabled()) {
      return EvaluationResult.pass();
    }
    return require(rule.type()).evaluate(text, rule);
  }
}
