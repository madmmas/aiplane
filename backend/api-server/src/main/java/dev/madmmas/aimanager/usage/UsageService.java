package dev.madmmas.aimanager.usage;

import dev.madmmas.aimanager.common.exception.BatchValidationException;
import dev.madmmas.aimanager.common.util.Ids;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.usage.dto.UsageEventCreateRequest;
import dev.madmmas.aimanager.usage.dto.UsageEventIngestRequest;
import dev.madmmas.aimanager.usage.dto.UsageEventIngestResponse;
import dev.madmmas.aimanager.usage.dto.UsageEventResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsageService {

  private final UsageEventRepository usageEventRepository;
  private final ProjectRepository projectRepository;

  public UsageService(
      UsageEventRepository usageEventRepository, ProjectRepository projectRepository) {
    this.usageEventRepository = usageEventRepository;
    this.projectRepository = projectRepository;
  }

  /**
   * All-or-nothing batched ingest. Validates every event first; on any failure rejects the whole
   * batch with a 400 listing per-index errors.
   */
  @Transactional
  public UsageEventIngestResponse ingest(UsageEventIngestRequest request) {
    if (request == null || request.events() == null || request.events().isEmpty()) {
      throw new IllegalArgumentException("events must be a non-empty array");
    }

    List<String> errors = new ArrayList<>();
    List<UsageEvent> prepared = new ArrayList<>(request.events().size());
    Set<String> knownProjects = new HashSet<>();
    Set<String> unknownProjects = new HashSet<>();

    for (int i = 0; i < request.events().size(); i++) {
      UsageEventCreateRequest item = request.events().get(i);
      if (item == null) {
        errors.add("[" + i + "] event must not be null");
        continue;
      }
      try {
        prepared.add(toEntity(item, knownProjects, unknownProjects));
      } catch (IllegalArgumentException ex) {
        errors.add("[" + i + "] " + ex.getMessage());
      }
    }

    if (!errors.isEmpty()) {
      throw new BatchValidationException(errors);
    }

    List<UsageEvent> saved = usageEventRepository.saveAll(prepared);
    List<UsageEventResponse> responses = saved.stream().map(UsageService::toResponse).toList();
    return new UsageEventIngestResponse(responses.size(), responses);
  }

  private UsageEvent toEntity(
      UsageEventCreateRequest request, Set<String> knownProjects, Set<String> unknownProjects) {
    if (request.projectId() == null || request.projectId().isBlank()) {
      throw new IllegalArgumentException("projectId is required");
    }
    if (request.model() == null || request.model().isBlank()) {
      throw new IllegalArgumentException("model is required");
    }

    String projectId = request.projectId().trim();
    if (unknownProjects.contains(projectId)) {
      throw new IllegalArgumentException("Unknown projectId: " + projectId);
    }
    if (!knownProjects.contains(projectId)) {
      if (!projectRepository.existsById(projectId)) {
        unknownProjects.add(projectId);
        throw new IllegalArgumentException("Unknown projectId: " + projectId);
      }
      knownProjects.add(projectId);
    }

    LlmProvider provider = LlmProvider.fromWireValue(request.provider());
    UsageEventStatus status = UsageEventStatus.fromWireValue(request.status());

    int inputTokens = request.inputTokens() == null ? 0 : request.inputTokens();
    int outputTokens = request.outputTokens() == null ? 0 : request.outputTokens();
    int latencyMs = request.latencyMs() == null ? 0 : request.latencyMs();
    BigDecimal costUsd = request.costUsd() == null ? BigDecimal.ZERO : request.costUsd();

    if (inputTokens < 0) {
      throw new IllegalArgumentException("inputTokens must be >= 0");
    }
    if (outputTokens < 0) {
      throw new IllegalArgumentException("outputTokens must be >= 0");
    }
    if (latencyMs < 0) {
      throw new IllegalArgumentException("latencyMs must be >= 0");
    }
    if (costUsd.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("costUsd must be >= 0");
    }

    UsageEvent event = new UsageEvent();
    String id =
        request.id() == null || request.id().isBlank() ? Ids.next("ue_") : request.id().trim();
    if (id.length() > 64) {
      throw new IllegalArgumentException("id must be at most 64 characters");
    }
    event.setId(id);
    event.setProjectId(projectId);
    event.setPromptId(blankToNull(request.promptId()));
    event.setPromptVersionId(blankToNull(request.promptVersionId()));
    event.setApiKeyId(blankToNull(request.apiKeyId()));
    event.setProvider(provider);
    event.setModel(request.model().trim());
    event.setInputTokens(inputTokens);
    event.setOutputTokens(outputTokens);
    event.setLatencyMs(latencyMs);
    event.setCostUsd(costUsd);
    event.setStatus(status);
    event.setTimestamp(request.timestamp() == null ? Instant.now() : request.timestamp());
    return event;
  }

  private static String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  static UsageEventResponse toResponse(UsageEvent event) {
    return new UsageEventResponse(
        event.getId(),
        event.getProjectId(),
        event.getPromptId(),
        event.getPromptVersionId(),
        event.getApiKeyId(),
        event.getProvider().wireValue(),
        event.getModel(),
        event.getInputTokens(),
        event.getOutputTokens(),
        event.getLatencyMs(),
        event.getCostUsd(),
        event.getStatus().wireValue(),
        event.getTimestamp());
  }
}
