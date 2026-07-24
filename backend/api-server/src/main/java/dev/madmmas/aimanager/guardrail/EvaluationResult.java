package dev.madmmas.aimanager.guardrail;

/**
 * Outcome of running a single {@link GuardrailEvaluator} against text.
 *
 * @param passed whether the text is allowed
 * @param reason human-readable explanation (empty when passed)
 * @param action action configured on the rule when failed; {@code null} when passed
 * @param matchedFragment optional snippet that triggered the failure
 * @param redactedText text with matches removed when action is {@link GuardrailAction#REDACT}
 */
public record EvaluationResult(
    boolean passed,
    String reason,
    GuardrailAction action,
    String matchedFragment,
    String redactedText) {

  public static EvaluationResult pass() {
    return new EvaluationResult(true, "", null, null, null);
  }

  public static EvaluationResult fail(String reason, GuardrailAction action) {
    return fail(reason, action, null, null);
  }

  public static EvaluationResult fail(
      String reason, GuardrailAction action, String matchedFragment, String redactedText) {
    return new EvaluationResult(false, reason, action, matchedFragment, redactedText);
  }

  public boolean shouldBlock() {
    return !passed && action == GuardrailAction.BLOCK;
  }
}
