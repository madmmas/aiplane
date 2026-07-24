package dev.madmmas.aimanager.prompt;

import dev.madmmas.aimanager.common.exception.ResourceNotFoundException;
import dev.madmmas.aimanager.prompt.dto.PlaygroundRunRequest;
import dev.madmmas.aimanager.prompt.dto.PlaygroundRunResponse;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlaygroundService {

  private final PromptRepository promptRepository;
  private final PromptVersionRepository versionRepository;
  private final PromptPlaygroundRunner playgroundRunner;

  public PlaygroundService(
      PromptRepository promptRepository,
      PromptVersionRepository versionRepository,
      PromptPlaygroundRunner playgroundRunner) {
    this.promptRepository = promptRepository;
    this.versionRepository = versionRepository;
    this.playgroundRunner = playgroundRunner;
  }

  @Transactional(readOnly = true)
  public PlaygroundRunResponse run(String promptId, PlaygroundRunRequest request) {
    Prompt prompt = requirePrompt(promptId);
    PromptVersion version = resolveVersion(prompt, request.versionId());

    LlmProvider provider = LlmProvider.fromWireValue(request.provider());
    String model = request.model();
    if (model == null || model.isBlank()) {
      throw new IllegalArgumentException("model is required");
    }

    Map<String, String> variables =
        request.variables() == null ? Map.of() : request.variables();
    String systemPrompt =
        PromptTemplateResolver.resolve(version.getSystemPrompt(), variables);
    String userPrompt =
        PromptTemplateResolver.resolve(version.getUserPromptTemplate(), variables);

    Double temperature = resolveTemperature(request.temperature(), version.getParameters());
    Integer maxTokens = resolveMaxTokens(request.maxTokens(), version.getParameters());

    CompletionResult result =
        playgroundRunner.run(
            new PlaygroundCommand(
                systemPrompt, userPrompt, provider, model, temperature, maxTokens));

    return new PlaygroundRunResponse(
        result.content(),
        result.inputTokens(),
        result.outputTokens(),
        result.latencyMs(),
        provider.wireValue(),
        model,
        result.blockedByGuardrail() ? Boolean.TRUE : null);
  }

  private PromptVersion resolveVersion(Prompt prompt, String versionId) {
    if (versionId != null && !versionId.isBlank()) {
      return versionRepository
          .findByIdAndPromptId(versionId, prompt.getId())
          .orElseThrow(
              () ->
                  new ResourceNotFoundException("Prompt version not found: " + versionId));
    }
    String activeId = prompt.getActiveVersionId();
    if (activeId == null || activeId.isBlank()) {
      throw new IllegalArgumentException(
          "versionId is required when the prompt has no active version");
    }
    return versionRepository
        .findByIdAndPromptId(activeId, prompt.getId())
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Active prompt version not found: " + activeId));
  }

  private Prompt requirePrompt(String id) {
    return promptRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Prompt not found: " + id));
  }

  static Double resolveTemperature(Double requestValue, Map<String, Object> parameters) {
    if (requestValue != null) {
      return requestValue;
    }
    Object fromParams = parameterValue(parameters, "temperature");
    if (fromParams instanceof Number number) {
      return number.doubleValue();
    }
    return null;
  }

  static Integer resolveMaxTokens(Integer requestValue, Map<String, Object> parameters) {
    if (requestValue != null) {
      return requestValue;
    }
    Object fromParams = parameterValue(parameters, "maxTokens", "max_tokens");
    if (fromParams instanceof Number number) {
      return number.intValue();
    }
    return null;
  }

  private static Object parameterValue(Map<String, Object> parameters, String... keys) {
    if (parameters == null || parameters.isEmpty()) {
      return null;
    }
    for (String key : keys) {
      if (parameters.containsKey(key)) {
        return parameters.get(key);
      }
    }
    return null;
  }
}
