package dev.madmmas.aimanager.guardrail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record GuardrailCreateRequest(
    @NotBlank String projectId,
    @NotBlank String name,
    @NotBlank String type,
    @NotBlank String stage,
    Map<String, Object> config,
    Boolean enabled,
    @NotBlank String action,
    String blockMessage) {}
