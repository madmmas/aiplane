package dev.madmmas.aimanager.guardrail.dto;

import java.util.Map;

public record GuardrailUpdateRequest(
    String name,
    String type,
    String stage,
    Map<String, Object> config,
    Boolean enabled,
    String action,
    String blockMessage) {}
