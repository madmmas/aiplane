package dev.madmmas.aimanager.usage;

/** Matches Flyway V8 {@code ck_usage_events_status} and {@code packages/types}. */
public enum UsageEventStatus {
  SUCCESS("success"),
  ERROR("error"),
  GUARDRAIL_BLOCKED("guardrail-blocked");

  private final String wireValue;

  UsageEventStatus(String wireValue) {
    this.wireValue = wireValue;
  }

  public String wireValue() {
    return wireValue;
  }

  public static UsageEventStatus fromWireValue(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("status is required");
    }
    for (UsageEventStatus status : values()) {
      if (status.wireValue.equals(value)) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown usage event status: " + value);
  }
}
