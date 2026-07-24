package dev.madmmas.aimanager.provider;

import dev.madmmas.aimanager.common.exception.ProviderNotConfiguredException;
import dev.madmmas.aimanager.prompt.LlmProvider;
import java.time.Duration;
import java.util.Optional;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Builds Spring AI {@link ChatModel} instances for Anthropic and OpenAI when API keys are
 * present. Missing keys do not prevent app startup — callers get a clear 503 instead.
 */
@Component
public class LlmProviderFactory {

  private final LlmProviderProperties providerProperties;
  private final PlaygroundProperties playgroundProperties;
  private final Optional<ChatModel> openAiChatModel;
  private final Optional<ChatModel> anthropicChatModel;

  public LlmProviderFactory(
      LlmProviderProperties providerProperties, PlaygroundProperties playgroundProperties) {
    this.providerProperties = providerProperties;
    this.playgroundProperties = playgroundProperties;
    this.openAiChatModel = buildOpenAiModel();
    this.anthropicChatModel = buildAnthropicModel();
  }

  public ChatModel getChatModel(LlmProvider provider) {
    return switch (provider) {
      case OPENAI ->
          openAiChatModel.orElseThrow(
              () ->
                  new ProviderNotConfiguredException(
                      "OpenAI provider is not configured (set OPENAI_API_KEY)"));
      case ANTHROPIC ->
          anthropicChatModel.orElseThrow(
              () ->
                  new ProviderNotConfiguredException(
                      "Anthropic provider is not configured (set ANTHROPIC_API_KEY)"));
      default ->
          throw new IllegalArgumentException(
              "Playground does not support provider: " + provider.wireValue()
                  + " (supported: anthropic, openai)");
    };
  }

  public boolean isConfigured(LlmProvider provider) {
    return switch (provider) {
      case OPENAI -> openAiChatModel.isPresent();
      case ANTHROPIC -> anthropicChatModel.isPresent();
      default -> false;
    };
  }

  private Optional<ChatModel> buildOpenAiModel() {
    if (!providerProperties.getOpenai().isConfigured()) {
      return Optional.empty();
    }
    OpenAiApi api =
        OpenAiApi.builder()
            .apiKey(providerProperties.getOpenai().getApiKey())
            .restClientBuilder(timedRestClientBuilder())
            .build();
    return Optional.of(OpenAiChatModel.builder().openAiApi(api).build());
  }

  private Optional<ChatModel> buildAnthropicModel() {
    if (!providerProperties.getAnthropic().isConfigured()) {
      return Optional.empty();
    }
    AnthropicApi api =
        AnthropicApi.builder()
            .apiKey(providerProperties.getAnthropic().getApiKey())
            .restClientBuilder(timedRestClientBuilder())
            .build();
    return Optional.of(AnthropicChatModel.builder().anthropicApi(api).build());
  }

  private RestClient.Builder timedRestClientBuilder() {
    Duration timeout = playgroundProperties.getTimeout();
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(timeout);
    requestFactory.setReadTimeout(timeout);
    return RestClient.builder().requestFactory(requestFactory);
  }
}
