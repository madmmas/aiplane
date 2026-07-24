package dev.madmmas.aimanager.provider;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aiplane.playground")
public class PlaygroundProperties {

  /** Read timeout for provider HTTP calls. */
  private Duration timeout = Duration.ofSeconds(30);

  public Duration getTimeout() {
    return timeout;
  }

  public void setTimeout(Duration timeout) {
    this.timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
  }
}
