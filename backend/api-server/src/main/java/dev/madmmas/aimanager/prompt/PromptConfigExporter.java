package dev.madmmas.aimanager.prompt;

/**
 * Hook invoked when a prompt version becomes {@link PromptVersionStatus#ACTIVE}.
 *
 * <p>Phase 5 will write the active prompt config into the Config Server backing store. Until then,
 * the default bean is a no-op / logging stub.
 */
public interface PromptConfigExporter {

  void onVersionActivated(Prompt prompt, PromptVersion version);
}
