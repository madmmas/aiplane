package dev.madmmas.aimanager.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when an LLM provider call fails for a non-timeout reason. */
@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class ProviderCallException extends RuntimeException {

  public ProviderCallException(String message, Throwable cause) {
    super(message, cause);
  }

  public ProviderCallException(String message) {
    super(message);
  }
}
