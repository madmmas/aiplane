package dev.madmmas.aimanager.guardrail;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.common.util.Ids;
import dev.madmmas.aimanager.guardrail.dto.EvaluatorResultResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailCreateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailUpdateRequest;
import dev.madmmas.aimanager.guardrail.evaluator.GuardrailEvaluatorRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GuardrailService {

  private final GuardrailRepository repository;
  private final GuardrailEvaluatorRegistry registry;

  public GuardrailService(GuardrailRepository repository, GuardrailEvaluatorRegistry registry) {
    this.repository = repository;
    this.registry = registry;
  }

  public List<GuardrailResponse> list(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      throw new IllegalArgumentException("projectId is required");
    }
    return repository.findByProjectId(projectId).stream().map(GuardrailService::toResponse).toList();
  }

  public GuardrailResponse get(String id) {
    return toResponse(require(id));
  }

  public GuardrailResponse create(GuardrailCreateRequest request) {
    if (repository.existsByProjectIdAndName(request.projectId(), request.name())) {
      throw new IllegalArgumentException("Guardrail name already exists in project");
    }
    validateConfig(request.type(), request.config());
    Guardrail guardrail =
        new Guardrail(
            Ids.next("gr_"),
            request.projectId(),
            request.name(),
            GuardrailType.fromWireValue(request.type()),
            GuardrailStage.fromWireValue(request.stage()),
            request.config() == null ? Map.of() : request.config(),
            request.enabled() == null || request.enabled(),
            GuardrailAction.fromWireValue(request.action()),
            request.blockMessage(),
            Instant.now());
    return toResponse(repository.save(guardrail));
  }

  public GuardrailResponse update(String id, GuardrailUpdateRequest request) {
    Guardrail existing = require(id);
    String name = request.name() != null ? request.name() : existing.name();
    if (!name.equals(existing.name())
        && repository.existsByProjectIdAndName(existing.projectId(), name)) {
      throw new IllegalArgumentException("Guardrail name already exists in project");
    }
    String typeWire = request.type() != null ? request.type() : existing.type().wireValue();
    Map<String, Object> config = request.config() != null ? request.config() : existing.config();
    validateConfig(typeWire, config);
    Guardrail updated =
        new Guardrail(
            existing.id(),
            existing.projectId(),
            name,
            GuardrailType.fromWireValue(typeWire),
            GuardrailStage.fromWireValue(
                request.stage() != null ? request.stage() : existing.stage().wireValue()),
            config,
            request.enabled() != null ? request.enabled() : existing.enabled(),
            GuardrailAction.fromWireValue(
                request.action() != null ? request.action() : existing.action().wireValue()),
            request.blockMessage() != null ? request.blockMessage() : existing.blockMessage(),
            existing.createdAt());
    return toResponse(repository.update(updated));
  }

  public void delete(String id) {
    if (!repository.deleteById(id)) {
      throw new ResourceNotFoundException("Guardrail not found: " + id);
    }
  }

  public EvaluatorResultResponse test(String id, String text) {
    Guardrail guardrail = require(id);
    EvaluationResult result = registry.evaluate(guardrail.toRule(), text);
    return toEvaluatorResult(guardrail, guardrail.stage(), result);
  }

  Guardrail require(String id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Guardrail not found: " + id));
  }

  private void validateConfig(String typeWire, Map<String, Object> config) {
    GuardrailType type = GuardrailType.fromWireValue(typeWire);
    if (type == GuardrailType.PII_DETECTION || type == GuardrailType.CUSTOM_LLM_JUDGE) {
      throw new IllegalArgumentException(
          "Guardrail type not implemented yet (Phase 6): " + type.wireValue());
    }
    GuardrailRule probe =
        new GuardrailRule(
            "validate",
            "validate",
            type,
            GuardrailStage.INPUT,
            config == null ? Map.of() : config,
            true,
            GuardrailAction.LOG_ONLY,
            null);
    registry.evaluate(probe, "");
  }

  static GuardrailResponse toResponse(Guardrail guardrail) {
    return new GuardrailResponse(
        guardrail.id(),
        guardrail.projectId(),
        guardrail.name(),
        guardrail.type().wireValue(),
        guardrail.stage().wireValue(),
        guardrail.config(),
        guardrail.enabled(),
        guardrail.action().wireValue(),
        guardrail.blockMessage(),
        guardrail.createdAt());
  }

  static EvaluatorResultResponse toEvaluatorResult(
      Guardrail guardrail, GuardrailStage stage, EvaluationResult result) {
    return new EvaluatorResultResponse(
        guardrail.id(),
        guardrail.name(),
        guardrail.type().wireValue(),
        stage.wireValue(),
        result.passed(),
        result.reason(),
        result.action() == null ? null : result.action().wireValue(),
        result.matchedFragment());
  }
}
