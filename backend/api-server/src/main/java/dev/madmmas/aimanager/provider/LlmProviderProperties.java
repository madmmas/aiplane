package dev.madmmas.aimanager.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.ai")
public class LlmProviderProperties {

  private final OpenAi openai = new OpenAi();
  private final Anthropic anthropic = new Anthropic();

  public OpenAi getOpenai() {
    return openai;
  }

  public Anthropic getAnthropic() {
    return anthropic;
  }

  public static class OpenAi {
    private String apiKey = "";

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey == null ? "" : apiKey;
    }

    public boolean isConfigured() {
      return apiKey != null && !apiKey.isBlank();
    }
  }

  public static class Anthropic {
    private String apiKey = "";

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey == null ? "" : apiKey;
    }

    public boolean isConfigured() {
      return apiKey != null && !apiKey.isBlank();
    }
  }
}
