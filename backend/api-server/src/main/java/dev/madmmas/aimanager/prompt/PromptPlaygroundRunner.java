package dev.madmmas.aimanager.prompt;

/**
 * Port for executing a resolved playground completion against an LLM provider.
 *
 * <p>Production bean uses Spring AI; unit tests mock this interface (no live API calls).
 */
public interface PromptPlaygroundRunner {

  CompletionResult run(PlaygroundCommand command);
}
