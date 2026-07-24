package dev.madmmas.aimanager.guardrail.dto;

import jakarta.validation.constraints.NotBlank;

public record GuardrailTestRequest(@NotBlank String text) {}
