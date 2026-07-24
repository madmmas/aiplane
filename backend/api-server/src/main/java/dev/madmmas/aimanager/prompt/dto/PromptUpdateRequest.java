package dev.madmmas.aimanager.prompt.dto;

import java.util.List;

public record PromptUpdateRequest(String name, String description, List<String> tags) {}
