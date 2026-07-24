package dev.madmmas.aimanager.guardrail;

import dev.madmmas.aimanager.guardrail.dto.EvaluatorResultResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailCreateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailTestRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/guardrails")
public class GuardrailController {

  private final GuardrailService service;

  public GuardrailController(GuardrailService service) {
    this.service = service;
  }

  @GetMapping
  List<GuardrailResponse> list(@RequestParam("projectId") String projectId) {
    return service.list(projectId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  GuardrailResponse create(@Valid @RequestBody GuardrailCreateRequest request) {
    return service.create(request);
  }

  @GetMapping("/{id}")
  GuardrailResponse get(@PathVariable("id") String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  GuardrailResponse update(
      @PathVariable("id") String id, @RequestBody GuardrailUpdateRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable("id") String id) {
    service.delete(id);
  }

  @PostMapping("/{id}/test")
  EvaluatorResultResponse test(
      @PathVariable("id") String id, @Valid @RequestBody GuardrailTestRequest request) {
    return service.test(id, request.text());
  }
}
