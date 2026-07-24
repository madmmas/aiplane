package dev.madmmas.aimanager.prompt.dto;

import java.time.Instant;
import java.util.List;

public record PromptResponse(
    String id,
    String projectId,
    String name,
    String description,
    List<String> tags,
    String activeVersionId,
    Instant createdAt,
    Instant updatedAt) {}
