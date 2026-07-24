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

class KeywordBlocklistEvaluatorTests {

  private final KeywordBlocklistEvaluator evaluator = new KeywordBlocklistEvaluator();

  @Test
  void passesWhenNoKeywordsMatch() {
    EvaluationResult result =
        evaluator.evaluate("hello world", rule(List.of("secret", "password")));

    assertThat(result.passed()).isTrue();
  }

  @Test
  void failsOnCaseInsensitiveSubstring() {
    EvaluationResult result = evaluator.evaluate("Drop the SECRET sauce", rule(List.of("secret")));

    assertThat(result.passed()).isFalse();
    assertThat(result.action()).isEqualTo(GuardrailAction.BLOCK);
    assertThat(result.matchedFragment()).isEqualToIgnoringCase("secret");
    assertThat(result.reason()).containsIgnoringCase("secret");
  }

  @Test
  void passesWhenKeywordListEmpty() {
    assertThat(evaluator.evaluate("anything", rule(List.of())).passed()).isTrue();
  }

  @Test
  void rejectsNonListKeywordsConfig() {
    GuardrailRule rule =
        new GuardrailRule(
            "g1",
            "bad",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", "not-a-list"),
            true,
            GuardrailAction.BLOCK,
            null);

    assertThatThrownBy(() -> evaluator.evaluate("x", rule))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("keywords");
  }

  private static GuardrailRule rule(List<String> keywords) {
    return new GuardrailRule(
        "g1",
        "blocklist",
        GuardrailType.KEYWORD_BLOCKLIST,
        GuardrailStage.INPUT,
        Map.of("keywords", keywords),
        true,
        GuardrailAction.BLOCK,
        null);
  }
}
