package dev.madmmas.aimanager.guardrail;

import dev.madmmas.aimanager.guardrail.dto.GuardrailSetCreateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetUpdateRequest;
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
@RequestMapping("/api/v1/guardrail-sets")
public class GuardrailSetController {

  private final GuardrailSetService service;

  public GuardrailSetController(GuardrailSetService service) {
    this.service = service;
  }

  @GetMapping
  List<GuardrailSetResponse> list(@RequestParam("projectId") String projectId) {
    return service.list(projectId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  GuardrailSetResponse create(@Valid @RequestBody GuardrailSetCreateRequest request) {
    return service.create(request);
  }

  @GetMapping("/{id}")
  GuardrailSetResponse get(@PathVariable("id") String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  GuardrailSetResponse update(
      @PathVariable("id") String id, @RequestBody GuardrailSetUpdateRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable("id") String id) {
    service.delete(id);
  }

  @PostMapping("/{id}/evaluate")
  GuardrailSetEvaluateResponse evaluate(
      @PathVariable("id") String id, @RequestBody GuardrailSetEvaluateRequest request) {
    return service.evaluate(id, request);
  }
}
