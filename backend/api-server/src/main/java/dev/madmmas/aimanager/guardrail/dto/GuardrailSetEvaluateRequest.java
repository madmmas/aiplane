package dev.madmmas.aimanager.guardrail.dto;

public record GuardrailSetEvaluateRequest(String input, String output, Boolean shortCircuitOnBlock) {}
