package dev.madmmas.aimanager.guardrail.dto;

import java.util.List;

public record GuardrailSetEvaluateResponse(
    boolean blocked, boolean shortCircuited, List<EvaluatorResultResponse> results) {}
