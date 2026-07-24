package dev.madmmas.aimanager.prompt;

/** Matches Flyway V3 {@code ck_prompt_versions_provider} and {@code packages/types}. */
public enum LlmProvider {
  ANTHROPIC("anthropic"),
  OPENAI("openai"),
  AZURE_OPENAI("azure-openai"),
  BEDROCK("bedrock"),
  OLLAMA("ollama"),
  GEMINI("gemini");

  private final String wireValue;

  LlmProvider(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static LlmProvider fromWireValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("provider is required");
    }
    for (LlmProvider provider : values()) {
      if (provider.wireValue.equals(value)) {
        return provider;
      }
    }
    throw new IllegalArgumentException("Unknown LLM provider: " + value);
  }
}
