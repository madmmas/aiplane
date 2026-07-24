package dev.madmmas.aimanager.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Body for {@code POST /api/v1/prompts/{id}/playground/run}.
 *
 * <p>{@code versionId} is optional — when null/blank the prompt's active version is used.
 * {@code temperature} / {@code maxTokens} override values from the version's {@code parameters}
 * map when set.
 */
public record PlaygroundRunRequest(
    String versionId,
    Map<String, String> variables,
    @NotBlank String provider,
    @NotBlank String model,
    Double temperature,
    Integer maxTokens) {}
