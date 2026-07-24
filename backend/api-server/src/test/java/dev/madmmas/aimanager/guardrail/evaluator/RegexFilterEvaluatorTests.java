package dev.madmmas.aimanager.guardrail.evaluator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.madmmas.aimanager.guardrail.EvaluationResult;
import dev.madmmas.aimanager.guardrail.GuardrailAction;
import dev.madmmas.aimanager.guardrail.GuardrailRule;
import dev.madmmas.aimanager.guardrail.GuardrailStage;
import dev.madmmas.aimanager.guardrail.GuardrailType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RegexFilterEvaluatorTests {

  private final RegexFilterEvaluator evaluator = new RegexFilterEvaluator();

  @Test
  void passesWhenNoPatternMatches() {
    EvaluationResult result = evaluator.evaluate("safe text", rule(List.of("\\d{3}-\\d{2}-\\d{4}")));

    assertThat(result.passed()).isTrue();
  }

  @Test
  void failsWhenPatternMatches() {
    EvaluationResult result =
        evaluator.evaluate("ssn 123-45-6789 here", rule(List.of("\\d{3}-\\d{2}-\\d{4}")));

    assertThat(result.passed()).isFalse();
    assertThat(result.matchedFragment()).isEqualTo("123-45-6789");
    assertThat(result.action()).isEqualTo(GuardrailAction.BLOCK);
  }

  @Test
  void rejectsOversizedPattern() {
    String huge = "a".repeat(RegexPatternGuard.MAX_PATTERN_LENGTH + 1);

    assertThatThrownBy(() -> evaluator.evaluate("x", rule(List.of(huge))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("max length");
  }

  @Test
  void rejectsNestedQuantifierPattern() {
    assertThatThrownBy(() -> evaluator.evaluate("aaaaaaaa", rule(List.of("(a+)+"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nested");
  }

  @Test
  void rejectsInvalidSyntax() {
    assertThatThrownBy(() -> evaluator.evaluate("x", rule(List.of("["))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid regex");
  }

  private static GuardrailRule rule(List<String> patterns) {
    return new GuardrailRule(
        "g1",
        "regex",
        GuardrailType.REGEX_FILTER,
        GuardrailStage.INPUT,
        Map.of("patterns", patterns),
        true,
        GuardrailAction.BLOCK,
        null);
  }
}
