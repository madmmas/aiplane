package dev.madmmas.aimanager.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an LLM provider call exceeds the configured timeout. */
@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class ProviderTimeoutException extends RuntimeException {

  public ProviderTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
