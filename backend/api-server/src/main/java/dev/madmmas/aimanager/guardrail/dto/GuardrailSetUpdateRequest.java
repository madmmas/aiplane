package dev.madmmas.aimanager.guardrail.dto;

import java.util.List;

public record GuardrailSetUpdateRequest(
    String name, Boolean shortCircuitOnBlock, List<String> guardrailIds) {}
