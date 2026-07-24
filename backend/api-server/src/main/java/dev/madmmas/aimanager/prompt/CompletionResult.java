package dev.madmmas.aimanager.prompt;

/** Result from {@link PromptPlaygroundRunner}. */
public record CompletionResult(
    String content,
    Integer inputTokens,
    Integer outputTokens,
    long latencyMs,
    boolean blockedByGuardrail) {}
