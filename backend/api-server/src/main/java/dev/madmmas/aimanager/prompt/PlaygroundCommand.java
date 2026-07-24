package dev.madmmas.aimanager.prompt;

/** Input to {@link PromptPlaygroundRunner}. */
public record PlaygroundCommand(
    String systemPrompt,
    String userPrompt,
    LlmProvider provider,
    String model,
    Double temperature,
    Integer maxTokens) {}
