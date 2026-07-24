package dev.madmmas.aimanager.guardrail;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.common.util.Ids;
import dev.madmmas.aimanager.guardrail.dto.EvaluatorResultResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetCreateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateRequest;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetEvaluateResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetResponse;
import dev.madmmas.aimanager.guardrail.dto.GuardrailSetUpdateRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class GuardrailSetService {

  private final GuardrailSetRepository setRepository;
  private final GuardrailRepository guardrailRepository;
  private final GuardrailSetEvaluationEngine evaluationEngine;

  public GuardrailSetService(
      GuardrailSetRepository setRepository,
      GuardrailRepository guardrailRepository,
      GuardrailSetEvaluationEngine evaluationEngine) {
    this.setRepository = setRepository;
    this.guardrailRepository = guardrailRepository;
    this.evaluationEngine = evaluationEngine;
  }

  public List<GuardrailSetResponse> list(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      throw new IllegalArgumentException("projectId is required");
    }
    return setRepository.findByProjectId(projectId).stream()
        .map(GuardrailSetService::toResponse)
        .toList();
  }

  public GuardrailSetResponse get(String id) {
    return toResponse(require(id));
  }

  public GuardrailSetResponse create(GuardrailSetCreateRequest request) {
    if (setRepository.existsByProjectIdAndName(request.projectId(), request.name())) {
      throw new IllegalArgumentException("Guardrail set name already exists in project");
    }
    List<String> memberIds = normalizeMemberIds(request.guardrailIds());
    validateMembersBelongToProject(request.projectId(), memberIds);
    GuardrailSet set =
        new GuardrailSet(
            Ids.next("gs_"),
            request.projectId(),
            request.name(),
            request.shortCircuitOnBlock() == null || request.shortCircuitOnBlock(),
            memberIds,
            Instant.now());
    return toResponse(setRepository.save(set));
  }

  public GuardrailSetResponse update(String id, GuardrailSetUpdateRequest request) {
    GuardrailSet existing = require(id);
    String name = request.name() != null ? request.name() : existing.name();
    if (!name.equals(existing.name())
        && setRepository.existsByProjectIdAndName(existing.projectId(), name)) {
      throw new IllegalArgumentException("Guardrail set name already exists in project");
    }
    List<String> memberIds =
        request.guardrailIds() != null
            ? normalizeMemberIds(request.guardrailIds())
            : existing.guardrailIds();
    validateMembersBelongToProject(existing.projectId(), memberIds);
    GuardrailSet updated =
        new GuardrailSet(
            existing.id(),
            existing.projectId(),
            name,
            request.shortCircuitOnBlock() != null
                ? request.shortCircuitOnBlock()
                : existing.shortCircuitOnBlock(),
            memberIds,
            existing.createdAt());
    return toResponse(setRepository.update(updated));
  }

  public void delete(String id) {
    if (!setRepository.deleteById(id)) {
      throw new ResourceNotFoundException("Guardrail set not found: " + id);
    }
  }

  public GuardrailSetEvaluateResponse evaluate(String id, GuardrailSetEvaluateRequest request) {
    GuardrailSet set = require(id);
    boolean shortCircuit =
        request.shortCircuitOnBlock() != null
            ? request.shortCircuitOnBlock()
            : set.shortCircuitOnBlock();
    List<Guardrail> ordered = loadOrderedMembers(set);
    return evaluationEngine.evaluate(
        ordered,
        request.input() == null ? "" : request.input(),
        request.output() == null ? "" : request.output(),
        shortCircuit);
  }

  private GuardrailSet require(String id) {
    return setRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Guardrail set not found: " + id));
  }

  private List<Guardrail> loadOrderedMembers(GuardrailSet set) {
    List<Guardrail> found = guardrailRepository.findByIds(set.guardrailIds());
    Map<String, Guardrail> byId = new HashMap<>();
    for (Guardrail guardrail : found) {
      byId.put(guardrail.id(), guardrail);
    }
    List<Guardrail> ordered = new ArrayList<>();
    for (String memberId : set.guardrailIds()) {
      Guardrail guardrail = byId.get(memberId);
      if (guardrail == null) {
        throw new IllegalStateException("Guardrail set member missing: " + memberId);
      }
      ordered.add(guardrail);
    }
    return ordered;
  }

  private void validateMembersBelongToProject(String projectId, List<String> memberIds) {
    if (memberIds.isEmpty()) {
      return;
    }
    List<Guardrail> found = guardrailRepository.findByIds(memberIds);
    if (found.size() != memberIds.size()) {
      Set<String> foundIds = new HashSet<>();
      for (Guardrail g : found) {
        foundIds.add(g.id());
      }
      List<String> missing = memberIds.stream().filter(id -> !foundIds.contains(id)).toList();
      throw new IllegalArgumentException("Unknown guardrail IDs: " + missing);
    }
    for (Guardrail guardrail : found) {
      if (!projectId.equals(guardrail.projectId())) {
        throw new IllegalArgumentException(
            "Guardrail " + guardrail.id() + " does not belong to project " + projectId);
      }
    }
  }

  private static List<String> normalizeMemberIds(List<String> guardrailIds) {
    if (guardrailIds == null) {
      return List.of();
    }
    Set<String> seen = new HashSet<>();
    List<String> ordered = new ArrayList<>();
    for (String id : guardrailIds) {
      if (id == null || id.isBlank()) {
        throw new IllegalArgumentException("guardrailIds must not contain blank values");
      }
      if (!seen.add(id)) {
        throw new IllegalArgumentException("Duplicate guardrailId in set: " + id);
      }
      ordered.add(id);
    }
    return ordered;
  }

  static GuardrailSetResponse toResponse(GuardrailSet set) {
    return new GuardrailSetResponse(
        set.id(),
        set.projectId(),
        set.name(),
        set.shortCircuitOnBlock(),
        set.guardrailIds(),
        set.createdAt());
  }
}
