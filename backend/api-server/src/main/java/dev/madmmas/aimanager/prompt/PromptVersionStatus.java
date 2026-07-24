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

  /**
   * Valid promotion path: draft → testing → active → archived. No skipping steps; archived is
   * terminal.
   */
  public boolean canTransitionTo(PromptVersionStatus target) {
    if (target == null || target == this) {
      return false;
    }
    return switch (this) {
      case DRAFT -> target == TESTING;
      case TESTING -> target == ACTIVE;
      case ACTIVE -> target == ARCHIVED;
      case ARCHIVED -> false;
    };
  }

  /** Next status when advancing one step along the promotion path, or empty if terminal. */
  public PromptVersionStatus nextPromotionStatus() {
    return switch (this) {
      case DRAFT -> TESTING;
      case TESTING -> ACTIVE;
      case ACTIVE, ARCHIVED -> null;
    };
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
