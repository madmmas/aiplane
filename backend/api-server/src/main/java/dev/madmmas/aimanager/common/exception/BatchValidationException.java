package dev.madmmas.aimanager.common.exception;

import java.util.List;

/** All-or-nothing batch validation failure with per-item error messages. */
public class BatchValidationException extends RuntimeException {

  private final List<String> errors;

  public BatchValidationException(List<String> errors) {
    super(errors == null || errors.isEmpty() ? "Validation failed" : String.join("; ", errors));
    this.errors = errors == null ? List.of() : List.copyOf(errors);
  }

  public List<String> getErrors() {
    return errors;
  }
}
