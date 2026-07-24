package dev.madmmas.aimanager.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptTemplateResolverTests {

  @Test
  void replacesKnownVariables() {
    String resolved =
        PromptTemplateResolver.resolve(
            "Hello {{ name }}, topic={{topic}}.", Map.of("name", "Ada", "topic", "AI"));
    assertThat(resolved).isEqualTo("Hello Ada, topic=AI.");
  }

  @Test
  void leavesUnknownPlaceholdersIntact() {
    String resolved =
        PromptTemplateResolver.resolve("Keep {{missing}} as-is", Map.of("other", "x"));
    assertThat(resolved).isEqualTo("Keep {{missing}} as-is");
  }

  @Test
  void handlesNullTemplateAndEmptyVariables() {
    assertThat(PromptTemplateResolver.resolve(null, Map.of())).isEqualTo("");
    assertThat(PromptTemplateResolver.resolve("plain", null)).isEqualTo("plain");
    assertThat(PromptTemplateResolver.resolve("plain", Map.of())).isEqualTo("plain");
  }
}
