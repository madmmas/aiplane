package dev.madmmas.aimanager.guardrail;

import static org.assertj.core.api.Assertions.assertThat;

import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateResponse;
import dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry;
import dev.madmmas.aimanager.guardrail.evaluator.KeywordBlocklistEvaluator;
import dev.madmmas.aimanager.guardrail.evaluator.MaxLengthEvaluator;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuardrailSetEvaluationEngineTests {

  private GuardrailSetEvaluationEngine engine;

  @BeforeEach
  void setUp() {
    engine =
        new GuardrailSetEvaluationEngine(
            new GuardrailEvaluatorRegistry(
                List.of(new KeywordBlocklistEvaluator(), new MaxLengthEvaluator())));
  }

  @Test
  void evaluatesInOrderAndShortCircuitsOnBlock() {
    Guardrail first =
        guardrail(
            "g1",
            "block-secret",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", List.of("secret")),
            GuardrailAction.BLOCK);
    Guardrail second =
        guardrail(
            "g2",
            "max",
            GuardrailType.MAX_LENGTH,
            GuardrailStage.INPUT,
            Map.of("maxChars", 1),
            GuardrailAction.BLOCK);

    GuardrailSetEvaluateResponse response =
        engine.evaluate(List.of(first, second), "top secret", "", true);

    assertThat(response.blocked()).isTrue();
    assertThat(response.shortCircuited()).isTrue();
    assertThat(response.results()).hasSize(1);
    assertThat(response.results().getFirst().guardrailId()).isEqualTo("g1");
  }

  @Test
  void continuesWhenShortCircuitDisabled() {
    Guardrail first =
        guardrail(
            "g1",
            "block-secret",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", List.of("secret")),
            GuardrailAction.BLOCK);
    Guardrail second =
        guardrail(
            "g2",
            "max",
            GuardrailType.MAX_LENGTH,
            GuardrailStage.INPUT,
            Map.of("maxChars", 1),
            GuardrailAction.BLOCK);

    GuardrailSetEvaluateResponse response =
        engine.evaluate(List.of(first, second), "top secret long", "", false);

    assertThat(response.blocked()).isTrue();
    assertThat(response.shortCircuited()).isFalse();
    assertThat(response.results()).hasSize(2);
  }

  @Test
  void skipsDisabledRules() {
    Guardrail disabled =
        new Guardrail(
            "g1",
            "proj",
            "off",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", List.of("secret")),
            false,
            GuardrailAction.BLOCK,
            null,
            Instant.now());

    GuardrailSetEvaluateResponse response =
        engine.evaluate(List.of(disabled), "secret", "", true);

    assertThat(response.blocked()).isFalse();
    assertThat(response.results()).isEmpty();
  }

  @Test
  void warnDoesNotShortCircuitEvenWhenEnabled() {
    Guardrail warn =
        guardrail(
            "g1",
            "warn-secret",
            GuardrailType.KEYWORD_BLOCKLIST,
            GuardrailStage.INPUT,
            Map.of("keywords", List.of("secret")),
            GuardrailAction.WARN);
    Guardrail block =
        guardrail(
            "g2",
            "max",
            GuardrailType.MAX_LENGTH,
            GuardrailStage.OUTPUT,
            Map.of("maxChars", 3),
            GuardrailAction.BLOCK);

    GuardrailSetEvaluateResponse response =
        engine.evaluate(List.of(warn, block), "secret", "toolong", true);

    assertThat(response.results()).hasSize(2);
    assertThat(response.blocked()).isTrue();
    assertThat(response.shortCircuited()).isTrue();
    assertThat(response.results().get(1).guardrailId()).isEqualTo("g2");
  }

  private static Guardrail guardrail(
      String id,
      String name,
      GuardrailType type,
      GuardrailStage stage,
      Map<String, Object> config,
      GuardrailAction action) {
    return new Guardrail(
        id, "proj", name, type, stage, config, true, action, null, Instant.now());
  }
}
