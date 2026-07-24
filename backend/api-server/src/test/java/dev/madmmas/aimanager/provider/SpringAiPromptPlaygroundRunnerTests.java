package dev.madmmas.aimanager.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import dev.madmmas.aimanager.common.exception.ProviderCallException;
import dev.madmmas.aimanager.common.exception.ProviderNotConfiguredException;
import dev.madmmas.aimanager.common.exception.ProviderTimeoutException;
import dev.madmmas.aimanager.prompt.CompletionResult;
import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.prompt.PlaygroundCommand;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
class SpringAiPromptPlaygroundRunnerTests {

  @Mock private LlmProviderFactory providerFactory;
  @Mock private ChatModel chatModel;

  private SpringAiPromptPlaygroundRunner runner;

  @BeforeEach
  void setUp() {
    runner = new SpringAiPromptPlaygroundRunner(providerFactory);
  }

  @Test
  void runReturnsContentAndUsageFromChatModel() {
    when(providerFactory.getChatModel(LlmProvider.OPENAI)).thenReturn(chatModel);
    ChatResponse response =
        ChatResponse.builder()
            .generations(
                java.util.List.of(
                    new org.springframework.ai.chat.model.Generation(
                        new org.springframework.ai.chat.messages.AssistantMessage("hi there"))))
            .metadata(
                org.springframework.ai.chat.metadata.ChatResponseMetadata.builder()
                    .usage(
                        new org.springframework.ai.chat.metadata.DefaultUsage(3, 7))
                    .build())
            .build();
    when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class))).thenReturn(response);

    CompletionResult result =
        runner.run(
            new PlaygroundCommand(
                "sys", "user", LlmProvider.OPENAI, "gpt-4o-mini", 0.1, 100));

    assertThat(result.content()).isEqualTo("hi there");
    assertThat(result.inputTokens()).isEqualTo(3);
    assertThat(result.outputTokens()).isEqualTo(7);
    assertThat(result.latencyMs()).isGreaterThanOrEqualTo(0L);
    assertThat(result.blockedByGuardrail()).isFalse();
  }

  @Test
  void runMapsMissingProviderToNotConfigured() {
    when(providerFactory.getChatModel(LlmProvider.ANTHROPIC))
        .thenThrow(new ProviderNotConfiguredException("Anthropic provider is not configured"));

    assertThrows(
        ProviderNotConfiguredException.class,
        () ->
            runner.run(
                new PlaygroundCommand(
                    "s", "u", LlmProvider.ANTHROPIC, "claude-sonnet-4-0", null, null)));
  }

  @Test
  void runMapsSocketTimeoutToGatewayTimeout() {
    when(providerFactory.getChatModel(LlmProvider.OPENAI)).thenReturn(chatModel);
    when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class)))
        .thenThrow(new ResourceAccessException("I/O", new SocketTimeoutException("read timed out")));

    assertThrows(
        ProviderTimeoutException.class,
        () ->
            runner.run(
                new PlaygroundCommand("s", "u", LlmProvider.OPENAI, "gpt-4o-mini", null, null)));
  }

  @Test
  void runMapsOtherFailuresToBadGateway() {
    when(providerFactory.getChatModel(LlmProvider.OPENAI)).thenReturn(chatModel);
    when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class)))
        .thenThrow(new RuntimeException("upstream 500"));

    assertThrows(
        ProviderCallException.class,
        () ->
            runner.run(
                new PlaygroundCommand("s", "u", LlmProvider.OPENAI, "gpt-4o-mini", null, null)));
  }

  @Test
  void factoryRejectsUnsupportedProvider() {
    LlmProviderProperties props = new LlmProviderProperties();
    PlaygroundProperties playground = new PlaygroundProperties();
    LlmProviderFactory factory = new LlmProviderFactory(props, playground);

    IllegalArgumentException error =
        assertThrows(
            IllegalArgumentException.class, () -> factory.getChatModel(LlmProvider.OLLAMA));
    assertThat(error).hasMessageContaining("does not support provider");
    assertThat(factory.isConfigured(LlmProvider.OPENAI)).isFalse();
    assertThat(factory.isConfigured(LlmProvider.ANTHROPIC)).isFalse();
  }

  @Test
  void factoryThrowsWhenOpenAiKeyMissing() {
    LlmProviderFactory factory =
        new LlmProviderFactory(new LlmProviderProperties(), new PlaygroundProperties());

    assertThrows(
        ProviderNotConfiguredException.class, () -> factory.getChatModel(LlmProvider.OPENAI));
  }
}
