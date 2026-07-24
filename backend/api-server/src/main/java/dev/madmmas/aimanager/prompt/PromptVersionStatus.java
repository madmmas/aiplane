package dev.madmmas.aimanager.prompt;

/** Matches Flyway V3 {@code ck_prompt_versions_status} and {@code packages/types}. */
public enum PromptVersionStatus {
  DRAFT("draft"),
  TESTING("testing"),
  ACTIVE("active"),
  ARCHIVED("archived");

  private final String wireValue;

  PromptVersionStatus(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static PromptVersionStatus fromWireValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("status is required");
    }
    for (PromptVersionStatus status : values()) {
      if (status.wireValue.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown prompt version status: " + value);
  }
}
