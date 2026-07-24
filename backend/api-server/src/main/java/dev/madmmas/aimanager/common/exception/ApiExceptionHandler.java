package dev.madmmas.aimanager.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException ex) {
    return body(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException ex) {
    return body(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .orElse("Validation failed");
    return body(HttpStatus.BAD_REQUEST, message);
  }

  private static ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("status", status.value());
    payload.put("error", status.getReasonPhrase());
    payload.put("message", message);
    return ResponseEntity.status(status).body(payload);
  }
}
