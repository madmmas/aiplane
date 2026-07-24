package dev.madmmas.aimanager.guardrail;

/** Matches Flyway V4 `ck_guardrails_type` and `packages/types` GuardrailType. */
public enum GuardrailType {
  KEYWORD_BLOCKLIST("keyword-blocklist"),
  REGEX_FILTER("regex-filter"),
  PII_DETECTION("pii-detection"),
  MAX_LENGTH("max-length"),
  CUSTOM_LLM_JUDGE("custom-llm-judge");

  private final String wireValue;

  GuardrailType(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static GuardrailType fromWireValue(String value) {
    for (GuardrailType type : values()) {
      if (type.wireValue.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown guardrail type: " + value);
  }
}
