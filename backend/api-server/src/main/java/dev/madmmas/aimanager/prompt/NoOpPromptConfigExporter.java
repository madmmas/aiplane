package dev.madmmas.aimanager.prompt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default {@link PromptConfigExporter} until Phase 5 implements the real Config Server write.
 * Logs at INFO so promotion is observable in local/dev without side effects.
 */
@Component
public class NoOpPromptConfigExporter implements PromptConfigExporter {

  private static final Logger log = LoggerFactory.getLogger(NoOpPromptConfigExporter.class);

  @Override
  public void onVersionActivated(Prompt prompt, PromptVersion version) {
    log.info(
        "Prompt version activated (Config Server export deferred to Phase 5): promptId={},"
            + " versionId={}, version={}, projectId={}",
        prompt.getId(),
        version.getId(),
        version.getVersion(),
        prompt.getProjectId());
  }
}
