package dev.madmmas.aimanager.guardrail.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailAction;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailStage;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MaxLengthEvaluatorTests {

  private final MaxLengthEvaluator evaluator = new MaxLengthEvaluator();

  @Test
  void passesWhenWithinLimit() {
    EvaluationResult result = evaluator.evaluate("hello", rule(10));

    assertThat(result.passed()).isTrue();
  }

  @Test
  void failsWhenOverLimit() {
    EvaluationResult result = evaluator.evaluate("abcdefghijk", rule(5));

    assertThat(result.passed()).isFalse();
    assertThat(result.reason()).contains("11").contains("5");
    assertThat(result.redactedText()).isEqualTo("abcde");
  }

  @Test
  void treatsNullTextAsEmpty() {
    assertThat(evaluator.evaluate(null, rule(0)).passed()).isTrue();
  }

  @Test
  void requiresMaxChars() {
    GuardrailRule rule =
        new GuardrailRule(
            "g1",
            "len",
            GuardrailType.MAX_LENGTH,
            GuardrailStage.INPUT,
            Map.of(),
            true,
            GuardrailAction.BLOCK,
            null);

    assertThatThrownBy(() -> evaluator.evaluate("x", rule))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("maxChars");
  }

  private static GuardrailRule rule(int maxChars) {
    return new GuardrailRule(
        "g1",
        "len",
        GuardrailType.MAX_LENGTH,
        GuardrailStage.BOTH,
        Map.of("maxChars", maxChars),
        true,
        GuardrailAction.BLOCK,
        null);
  }
}
