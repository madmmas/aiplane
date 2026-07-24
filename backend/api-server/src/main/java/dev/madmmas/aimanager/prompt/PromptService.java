package dev.madmmas.aimanager.prompt;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.common.util.Ids;
import dev.madmmas.aimanager.project.ProjectRepository;
import dev.madmmas.aimanager.prompt.dto.PromptCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptResponse;
import dev.madmmas.aimanager.prompt.dto.PromptUpdateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionCreateRequest;
import dev.madmmas.aimanager.prompt.dto.PromptVersionResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromptService {

  private static final String DEFAULT_CREATED_BY = "system";

  private final PromptRepository promptRepository;
  private final PromptVersionRepository versionRepository;
  private final ProjectRepository projectRepository;
  private final PromptConfigExporter configExporter;

  public PromptService(
      PromptRepository promptRepository,
      PromptVersionRepository versionRepository,
      ProjectRepository projectRepository,
      PromptConfigExporter configExporter) {
    this.promptRepository = promptRepository;
    this.versionRepository = versionRepository;
    this.projectRepository = projectRepository;
    this.configExporter = configExporter;
  }

  @Transactional(readOnly = true)
  public List<PromptResponse> list(String projectId) {
    if (projectId == null || projectId.isBlank()) {
      throw new IllegalArgumentException("projectId is required");
    }
    return promptRepository.findByProjectIdOrderByUpdatedAtDesc(projectId).stream()
        .map(PromptService::toPromptResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PromptResponse get(String id) {
    return toPromptResponse(requirePrompt(id));
  }

  @Transactional
  public PromptResponse create(PromptCreateRequest request) {
    if (!projectRepository.existsById(request.projectId())) {
      throw new IllegalArgumentException("Unknown projectId: " + request.projectId());
    }
    if (promptRepository.existsByProjectIdAndName(request.projectId(), request.name())) {
      throw new IllegalArgumentException("Prompt name already exists in project");
    }
    Prompt prompt = new Prompt();
    prompt.setId(Ids.next("prm_"));
    prompt.setProjectId(request.projectId());
    prompt.setName(request.name());
    prompt.setDescription(request.description());
    prompt.setTags(toTagArray(request.tags()));
    return toPromptResponse(promptRepository.save(prompt));
  }

  @Transactional
  public PromptResponse update(String id, PromptUpdateRequest request) {
    Prompt existing = requirePrompt(id);
    if (request.name() != null && !request.name().isBlank()) {
      if (!request.name().equals(existing.getName())
          && promptRepository.existsByProjectIdAndNameAndIdNot(
              existing.getProjectId(), request.name(), id)) {
        throw new IllegalArgumentException("Prompt name already exists in project");
      }
      existing.setName(request.name());
    }
    if (request.description() != null) {
      existing.setDescription(request.description());
    }
    if (request.tags() != null) {
      existing.setTags(toTagArray(request.tags()));
    }
    return toPromptResponse(promptRepository.save(existing));
  }

  @Transactional
  public void delete(String id) {
    if (!promptRepository.existsById(id)) {
      throw new ResourceNotFoundException("Prompt not found: " + id);
    }
    promptRepository.deleteById(id);
  }

  @Transactional(readOnly = true)
  public List<PromptVersionResponse> listVersions(String promptId) {
    requirePrompt(promptId);
    return versionRepository.findByPromptIdOrderByVersionDesc(promptId).stream()
        .map(PromptService::toVersionResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public PromptVersionResponse getVersion(String promptId, String versionId) {
    requirePrompt(promptId);
    return toVersionResponse(
        versionRepository
            .findByIdAndPromptId(versionId, promptId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Prompt version not found: " + versionId)));
  }

  @Transactional
  public PromptVersionResponse createVersion(String promptId, PromptVersionCreateRequest request) {
    requirePrompt(promptId);
    LlmProvider provider = LlmProvider.fromWireValue(request.provider());
    int nextVersion = versionRepository.findMaxVersionByPromptId(promptId) + 1;
    String createdBy =
        request.createdBy() == null || request.createdBy().isBlank()
            ? DEFAULT_CREATED_BY
            : request.createdBy();

    PromptVersion version = new PromptVersion();
    version.setId(Ids.next("ver_"));
    version.setPromptId(promptId);
    version.setVersion(nextVersion);
    version.setLabel(request.label());
    version.setModel(request.model());
    version.setProvider(provider);
    version.setSystemPrompt(request.systemPrompt() == null ? "" : request.systemPrompt());
    version.setUserPromptTemplate(
        request.userPromptTemplate() == null ? "" : request.userPromptTemplate());
    version.setParameters(
        request.parameters() == null
            ? new HashMap<>()
            : new HashMap<>(request.parameters()));
    version.setStatus(PromptVersionStatus.DRAFT);
    version.setCreatedBy(createdBy);
    return toVersionResponse(versionRepository.save(version));
  }

  /**
   * Applies a status transition for a version. Valid path: draft → testing → active → archived.
   * Activating a version archives any other active version for the same prompt and updates {@code
   * prompts.active_version_id}, then invokes {@link PromptConfigExporter}.
   */
  @Transactional
  public PromptVersionResponse updateVersionStatus(
      String promptId, String versionId, String statusWire) {
    PromptVersionStatus target = PromptVersionStatus.fromWireValue(statusWire);
    return applyStatusTransition(promptId, versionId, target);
  }

  /** Advances one step along the promotion path (draft → testing → active). */
  @Transactional
  public PromptVersionResponse promoteVersion(String promptId, String versionId) {
    PromptVersion version = requireVersion(promptId, versionId);
    PromptVersionStatus next = version.getStatus().nextPromotionStatus();
    if (next == null) {
      throw new IllegalArgumentException(
          "Cannot promote from status: " + version.getStatus().wireValue());
    }
    return applyStatusTransition(promptId, versionId, next);
  }

  private PromptVersionResponse applyStatusTransition(
      String promptId, String versionId, PromptVersionStatus target) {
    Prompt prompt = requirePrompt(promptId);
    PromptVersion version = requireVersion(promptId, versionId);
    PromptVersionStatus current = version.getStatus();

    if (!current.canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "Invalid status transition: " + current.wireValue() + " → " + target.wireValue());
    }

    if (target == PromptVersionStatus.ACTIVE) {
      archiveOtherActiveVersions(promptId, versionId);
      version.setStatus(PromptVersionStatus.ACTIVE);
      versionRepository.save(version);
      prompt.setActiveVersionId(versionId);
      promptRepository.save(prompt);
      configExporter.onVersionActivated(prompt, version);
      return toVersionResponse(version);
    }

    version.setStatus(target);
    versionRepository.save(version);

    if (target == PromptVersionStatus.ARCHIVED
        && versionId.equals(prompt.getActiveVersionId())) {
      prompt.setActiveVersionId(null);
      promptRepository.save(prompt);
    }

    return toVersionResponse(version);
  }

  private void archiveOtherActiveVersions(String promptId, String keepVersionId) {
    List<PromptVersion> active =
        versionRepository.findByPromptIdAndStatus(promptId, PromptVersionStatus.ACTIVE);
    for (PromptVersion other : active) {
      if (!other.getId().equals(keepVersionId)) {
        other.setStatus(PromptVersionStatus.ARCHIVED);
        versionRepository.save(other);
      }
    }
  }

  private Prompt requirePrompt(String id) {
    return promptRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Prompt not found: " + id));
  }

  private PromptVersion requireVersion(String promptId, String versionId) {
    requirePrompt(promptId);
    return versionRepository
        .findByIdAndPromptId(versionId, promptId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Prompt version not found: " + versionId));
  }

  private static String[] toTagArray(List<String> tags) {
    if (tags == null || tags.isEmpty()) {
      return new String[0];
    }
    return tags.toArray(String[]::new);
  }

  static PromptResponse toPromptResponse(Prompt prompt) {
    List<String> tags =
        prompt.getTags() == null ? List.of() : Arrays.asList(prompt.getTags());
    return new PromptResponse(
        prompt.getId(),
        prompt.getProjectId(),
        prompt.getName(),
        prompt.getDescription(),
        tags,
        prompt.getActiveVersionId(),
        prompt.getCreatedAt(),
        prompt.getUpdatedAt());
  }

  static PromptVersionResponse toVersionResponse(PromptVersion version) {
    Map<String, Object> parameters =
        version.getParameters() == null ? Map.of() : version.getParameters();
    return new PromptVersionResponse(
        version.getId(),
        version.getPromptId(),
        version.getVersion(),
        version.getLabel(),
        version.getModel(),
        version.getProvider().wireValue(),
        version.getSystemPrompt(),
        version.getUserPromptTemplate(),
        parameters,
        version.getStatus().wireValue(),
        version.getCreatedBy(),
        version.getCreatedAt(),
        version.getMetrics());
  }
}
