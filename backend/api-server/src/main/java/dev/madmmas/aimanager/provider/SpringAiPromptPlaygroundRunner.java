package dev.madmmas.aimanager.provider;

import dev.madmmas.aimanager.common.exception.ProviderCallException;
import dev.madmmas.aimanager.common.exception.ProviderTimeoutException;
import dev.madmmas.aimanager.prompt.CompletionResult;
import dev.madmmas.aimanager.prompt.LlmProvider;
import dev.madmmas.aimanager.prompt.PlaygroundCommand;
import dev.madmmas.aimanager.prompt.PromptPlaygroundRunner;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Production {@link PromptPlaygroundRunner} using Spring AI {@link ChatClient}. Mock this
 * interface in unit tests — never call live providers from CI.
 */
@Component
@Primary
public class SpringAiPromptPlaygroundRunner implements PromptPlaygroundRunner {

  private final LlmProviderFactory providerFactory;

  public SpringAiPromptPlaygroundRunner(LlmProviderFactory providerFactory) {
    this.providerFactory = providerFactory;
  }

  @Override
  public CompletionResult run(PlaygroundCommand command) {
    ChatModel chatModel = providerFactory.getChatModel(command.provider());
    ChatClient client = ChatClient.create(chatModel);
    ChatOptions options = buildOptions(command);

    long start = System.currentTimeMillis();
    try {
      ChatClient.ChatClientRequestSpec spec =
          client.prompt().system(nullToEmpty(command.systemPrompt())).user(nullToEmpty(command.userPrompt()));
      if (options != null) {
        spec = spec.options(options);
      }
      ChatResponse response = spec.call().chatResponse();
      long latencyMs = System.currentTimeMillis() - start;
      return toResult(response, latencyMs);
    } catch (ResourceAccessException ex) {
      throw mapTimeoutOrCall(ex, command.provider());
    } catch (RestClientException ex) {
      throw new ProviderCallException(
          "LLM provider call failed (" + command.provider().wireValue() + "): " + ex.getMessage(),
          ex);
    } catch (RuntimeException ex) {
      if (isTimeout(ex)) {
        throw new ProviderTimeoutException(
            "LLM provider timed out (" + command.provider().wireValue() + ")", ex);
      }
      throw new ProviderCallException(
          "LLM provider call failed (" + command.provider().wireValue() + "): " + ex.getMessage(),
          ex);
    }
  }

  private static ChatOptions buildOptions(PlaygroundCommand command) {
    boolean hasTemp = command.temperature() != null;
    boolean hasMax = command.maxTokens() != null;
    if (!hasTemp && !hasMax && command.model() == null) {
      return null;
    }
    return switch (command.provider()) {
      case OPENAI -> {
        OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder().model(command.model());
        if (hasTemp) {
          builder.temperature(command.temperature());
        }
        if (hasMax) {
          builder.maxTokens(command.maxTokens());
        }
        yield builder.build();
      }
      case ANTHROPIC -> {
        AnthropicChatOptions.Builder builder =
            AnthropicChatOptions.builder().model(command.model());
        if (hasTemp) {
          builder.temperature(command.temperature());
        }
        if (hasMax) {
          builder.maxTokens(command.maxTokens());
        } else {
          // Anthropic requires max_tokens; default when neither request nor version set it.
          builder.maxTokens(1024);
        }
        yield builder.build();
      }
      default ->
          throw new IllegalArgumentException(
              "Playground does not support provider: " + command.provider().wireValue());
    };
  }

  private static CompletionResult toResult(ChatResponse response, long latencyMs) {
    String content = "";
    if (response != null && response.getResult() != null && response.getResult().getOutput() != null) {
      content = nullToEmpty(response.getResult().getOutput().getText());
    }
    Integer inputTokens = null;
    Integer outputTokens = null;
    if (response != null && response.getMetadata() != null) {
      Usage usage = response.getMetadata().getUsage();
      if (usage != null) {
        inputTokens = usage.getPromptTokens();
        outputTokens = usage.getCompletionTokens();
      }
    }
    return new CompletionResult(content, inputTokens, outputTokens, latencyMs, false);
  }

  private static RuntimeException mapTimeoutOrCall(ResourceAccessException ex, LlmProvider provider) {
    if (isTimeout(ex)) {
      return new ProviderTimeoutException(
          "LLM provider timed out (" + provider.wireValue() + ")", ex);
    }
    return new ProviderCallException(
        "LLM provider call failed (" + provider.wireValue() + "): " + ex.getMessage(), ex);
  }

  private static boolean isTimeout(Throwable ex) {
    Throwable current = ex;
    while (current != null) {
      if (current instanceof SocketTimeoutException
          || current instanceof TimeoutException
          || current instanceof InterruptedIOException) {
        return true;
      }
      String message = current.getMessage();
      if (message != null && message.toLowerCase().contains("timed out")) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }

  private static String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
