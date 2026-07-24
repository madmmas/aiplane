package dev.madmmas.aimanager.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an LLM provider is selected but no API key / bean is configured. */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ProviderNotConfiguredException extends RuntimeException {

  public ProviderNotConfiguredException(String message) {
    super(message);
  }
}
