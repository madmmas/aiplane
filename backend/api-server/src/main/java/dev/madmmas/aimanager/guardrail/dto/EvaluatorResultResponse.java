package dev.madmmas.aimanager.guardrail.dto;

public record EvaluatorResultResponse(
    String guardrailId,
    String name,
    String type,
    String stage,
    boolean passed,
    String reason,
    String action,
    String matchedFragment) {}
