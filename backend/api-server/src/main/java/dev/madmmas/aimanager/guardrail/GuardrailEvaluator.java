package dev.madmmas.aimanager.guardrail;

/**
 * Strategy for evaluating text against a single guardrail rule.
 *
 * <p>Implementations are registered by {@link
 * dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry} and invoked from the
 * Spring AI {@link dev.madmmas.aimanager.guardrail.advisor.GuardrailCallAdvisor}.
 */
public interface GuardrailEvaluator {

  /** Guardrail type this strategy handles. */
  GuardrailType supportedType();

  /**
   * Evaluate {@code text} using the rule's config and action.
   *
   * @param text prompt or response text
   * @param rule rule under evaluation (must match {@link #supportedType()})
   */
  EvaluationResult evaluate(String text, GuardrailRule rule);
}
