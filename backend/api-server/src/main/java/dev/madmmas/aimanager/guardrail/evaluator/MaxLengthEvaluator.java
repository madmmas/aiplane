package dev.madmmas.aimanager.guardrail.evaluator;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailEvaluator;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Fails when text length exceeds {@code maxChars}. */
@Component
public class MaxLengthEvaluator implements GuardrailEvaluator {

  @Override
  public GuardrailType supportedType() {
    return GuardrailType.MAX_LENGTH;
  }

  @Override
  public EvaluationResult evaluate(String text, GuardrailRule rule) {
    int maxChars = readMaxChars(rule.config());
    int length = text == null ? 0 : text.length();
    if (length <= maxChars) {
      return EvaluationResult.pass();
    }
    String message =
        rule.blockMessage() != null && !rule.blockMessage().isBlank()
            ? rule.blockMessage()
            : "Text length " + length + " exceeds max of " + maxChars;
    String redacted = text == null ? "" : text.substring(0, Math.max(0, maxChars));
    return EvaluationResult.fail(message, rule.action(), null, redacted);
  }

  static int readMaxChars(Map<String, Object> config) {
    Object raw = config.get("maxChars");
    if (raw == null) {
      throw new IllegalArgumentException("max-length config.maxChars is required");
    }
    if (raw instanceof Number number) {
      int value = number.intValue();
      if (value < 0) {
        throw new IllegalArgumentException("maxChars must be >= 0");
      }
      return value;
    }
    try {
      int value = Integer.parseInt(String.valueOf(raw));
      if (value < 0) {
        throw new IllegalArgumentException("maxChars must be >= 0");
      }
      return value;
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("maxChars must be a number", ex);
    }
  }
}
