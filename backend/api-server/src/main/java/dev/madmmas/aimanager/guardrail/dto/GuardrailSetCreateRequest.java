package dev.madmmas.aimanager.guardrail.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record GuardrailSetCreateRequest(
    @NotBlank String projectId,
    @NotBlank String name,
    Boolean shortCircuitOnBlock,
    List<String> guardrailIds) {}
