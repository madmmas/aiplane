package dev.madmmas.aimanager.guardrail;

/** Action taken when an evaluator fails (SPEC §5.3). */
public enum GuardrailAction {
  BLOCK("block"),
  WARN("warn"),
  REDACT("redact"),
  LOG_ONLY("log-only");

  private final String wireValue;

  GuardrailAction(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static GuardrailAction fromWireValue(String value) {
    for (GuardrailAction action : values()) {
      if (action.wireValue.equals(value)) {
        return action;
      }
    }
    throw new IllegalArgumentException("Unknown guardrail action: " + value);
  }
}
