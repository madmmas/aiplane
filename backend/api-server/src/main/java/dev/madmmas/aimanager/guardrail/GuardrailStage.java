package dev.madmmas.aimanager.guardrail;

/** When a rule applies relative to the LLM call. */
public enum GuardrailStage {
  INPUT("input"),
  OUTPUT("output"),
  BOTH("both");

  private final String wireValue;

  GuardrailStage(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public boolean appliesToInput() {
    return this == INPUT || this == BOTH;
  }

  public boolean appliesToOutput() {
    return this == OUTPUT || this == BOTH;
  }

  public static GuardrailStage fromWireValue(String value) {
    for (GuardrailStage stage : values()) {
      if (stage.wireValue.equals(value)) {
        return stage;
      }
    }
    throw new IllegalArgumentException("Unknown guardrail stage: " + value);
  }
}
