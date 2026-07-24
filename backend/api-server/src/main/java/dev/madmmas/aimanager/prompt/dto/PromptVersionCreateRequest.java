package dev.madmmas.aimanager.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record PromptVersionCreateRequest(
    String label,
    @NotBlank String model,
    @NotBlank String provider,
    String systemPrompt,
    String userPromptTemplate,
    Map<String, Object> parameters,
    String createdBy) {}
