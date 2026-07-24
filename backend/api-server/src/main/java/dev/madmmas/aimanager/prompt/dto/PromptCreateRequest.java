package dev.madmmas.aimanager.prompt.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record PromptCreateRequest(
    @NotBlank String projectId,
    @NotBlank String name,
    String description,
    List<String> tags) {}
