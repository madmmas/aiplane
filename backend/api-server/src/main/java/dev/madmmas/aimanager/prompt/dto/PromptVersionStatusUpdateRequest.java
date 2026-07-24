package dev.madmmas.aimanager.prompt.dto;

import jakarta.validation.constraints.NotBlank;

public record PromptVersionStatusUpdateRequest(@NotBlank String status) {}
